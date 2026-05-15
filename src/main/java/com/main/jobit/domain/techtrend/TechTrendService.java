package com.main.jobit.techtrend;

import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.MediaModule;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.Thumbnail;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TechTrendService {

    private final TechArticleRepository repository;

    private static final int ARTICLES_PER_SOURCE = 5;
    private static final long REFRESH_INTERVAL_MS = 3L * 24 * 60 * 60 * 1000;  // 3일
    private static final long INITIAL_DELAY_MS = 30_000L;                       // 30초
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final Pattern IMG_PATTERN =
        Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    private static final String[][] TAG_RULES = {
        {"AI/ML",      "ai,llm,gpt,머신러닝,딥러닝,machine learning,chatgpt,vector,벡터,embedding"},
        {"Kubernetes", "kubernetes,k8s,docker,컨테이너,container,helm,argo"},
        {"Kafka",      "kafka,카프카,rabbitmq,이벤트 드리븐,event driven,메시지 큐"},
        {"Redis",      "redis,레디스,캐시,cache"},
        {"Database",   "mysql,postgresql,mongodb,sql,쿼리,인덱스,database,데이터베이스"},
        {"Java",       "java,spring,jvm,kotlin,자바,스프링,virtual thread"},
        {"TypeScript", "typescript,타입스크립트"},
        {"React",      "react,vue,angular,next.js,nextjs,프론트,frontend,svelte,nuxt"},
        {"Python",     "python,파이썬,django,fastapi"},
    };

    /* ─── 조회 ─── */

    @Cacheable("techTrends")
    @Transactional(readOnly = true)
    public List<TechArticleDto> getLatest() {
        return repository.findTop8ByOrderByPublishedAtDesc().stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public TechArticleDto getById(UUID id) {
        TechArticle article = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("기사를 찾을 수 없습니다."));
        return toDto(article);
    }

    /* ─── 스케줄러 (3일마다 RSS 동기화) ─── */

    @Scheduled(fixedRate = REFRESH_INTERVAL_MS, initialDelay = INITIAL_DELAY_MS)
    @CacheEvict(value = "techTrends", allEntries = true)
    @Transactional
    public void refreshArticles() {
        log.info("Tech trends refresh started");
        int upserted = 0;
        for (RssSource source : RssSource.values()) {
            for (TechArticle article : fetchRss(source)) {
                upsert(article);
                upserted++;
            }
        }
        log.info("Tech trends refresh completed: {} articles upserted", upserted);
    }

    private void upsert(TechArticle fetched) {
        repository.findByUrl(fetched.getUrl())
            .ifPresentOrElse(
                existing -> existing.update(
                    fetched.getTitle(), fetched.getDescription(), fetched.getContent(),
                    fetched.getTag(), fetched.getImageUrl(), fetched.getPublishedAt()
                ),
                () -> repository.save(fetched)
            );
    }

    /* ─── RSS Fetch ─── */

    private List<TechArticle> fetchRss(RssSource source) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(source.getUrl()).toURL().openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; JobIT/1.0)");
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(10_000);

            SyndFeedInput input = new SyndFeedInput();
            try (InputStream is = conn.getInputStream();
                 XmlReader reader = new XmlReader(is)) {
                SyndFeed feed = input.build(reader);
                return feed.getEntries().stream()
                    .limit(ARTICLES_PER_SOURCE)
                    .map(e -> toEntity(e, source))
                    .toList();
            }
        } catch (Exception e) {
            log.warn("RSS fetch failed [{}]: {}", source.getName(), e.getMessage());
            return List.of();
        }
    }

    private TechArticle toEntity(SyndEntry entry, RssSource source) {
        String content = extractContent(entry);
        String rawDesc = entry.getDescription() != null ? entry.getDescription().getValue() : content;
        String desc = stripHtml(rawDesc);
        if (desc.length() > 250) desc = desc.substring(0, 250) + "...";

        String title = entry.getTitle() != null ? entry.getTitle().trim() : "";
        LocalDateTime publishedAt = entry.getPublishedDate() != null
            ? LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), KST)
            : LocalDateTime.now();

        String imageUrl = extractImageUrl(entry, content, rawDesc);
        if (imageUrl != null) imageUrl = resolveUrl(imageUrl, entry.getLink());

        return TechArticle.builder()
            .url(entry.getLink())
            .title(title)
            .source(source.getName())
            .description(desc)
            .content(content)
            .tag(detectTag(title, desc))
            .imageUrl(imageUrl)
            .publishedAt(publishedAt)
            .build();
    }

    private String extractContent(SyndEntry entry) {
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            for (SyndContent c : entry.getContents()) {
                if (c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            return entry.getDescription().getValue();
        }
        return "";
    }

    private String extractImageUrl(SyndEntry entry, String content, String description) {
        // 1. media:thumbnail / media:content
        MediaEntryModule media = (MediaEntryModule) entry.getModule(MediaModule.URI);
        if (media != null) {
            if (media.getMetadata() != null && media.getMetadata().getThumbnail() != null) {
                for (Thumbnail t : media.getMetadata().getThumbnail()) {
                    if (t.getUrl() != null) return t.getUrl().toString();
                }
            }
            if (media.getMediaContents() != null) {
                for (MediaContent mc : media.getMediaContents()) {
                    if (mc.getReference() != null) return mc.getReference().toString();
                }
            }
        }

        // 2. enclosure (image/*)
        if (entry.getEnclosures() != null) {
            for (SyndEnclosure enc : entry.getEnclosures()) {
                if (enc.getType() != null && enc.getType().startsWith("image/") && enc.getUrl() != null) {
                    return enc.getUrl();
                }
            }
        }

        // 3. content/description 안의 첫 <img>
        String html = (content != null ? content : "") + " " + (description != null ? description : "");
        Matcher m = IMG_PATTERN.matcher(html);
        if (m.find()) return m.group(1);

        return null;
    }

    private String resolveUrl(String src, String articleUrl) {
        if (src == null || src.isBlank()) return null;
        if (src.startsWith("//")) return "https:" + src;
        if (src.startsWith("http://") || src.startsWith("https://")) return src;
        if (articleUrl != null) {
            try {
                return URI.create(articleUrl).resolve(src).toString();
            } catch (Exception ignore) {
                return null;
            }
        }
        return null;
    }

    /* ─── 변환 / 유틸 ─── */

    private TechArticleDto toDto(TechArticle a) {
        return new TechArticleDto(
            a.getId(),
            a.getTitle(),
            a.getSource(),
            a.getDescription(),
            a.getContent(),
            a.getTag(),
            a.getUrl(),
            a.getImageUrl(),
            a.getPublishedAt() != null ? a.getPublishedAt().format(DATE_FMT) : ""
        );
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", " ")
                   .replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                   .replaceAll("&gt;", ">").replaceAll("&nbsp;|&#[0-9]+;", " ")
                   .replaceAll("\\s+", " ").trim();
    }

    private String detectTag(String title, String desc) {
        String text = (title + " " + desc).toLowerCase();
        for (String[] rule : TAG_RULES) {
            for (String kw : rule[1].split(",")) {
                if (text.contains(kw.trim())) return rule[0];
            }
        }
        return "기술";
    }
}