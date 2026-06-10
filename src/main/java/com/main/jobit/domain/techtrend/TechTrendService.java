package com.main.jobit.domain.techtrend;

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

// 기술 블로그 RSS/Atom 피드를 주기적으로 수집·정규화·태깅해 DB에 적재하고,
// 화면 조회용으로 캐시 제공까지 담당하는 도메인 서비스.
// 외부 의존: ROME(SyndFeed)으로 피드 파싱, HttpURLConnection으로 직접 fetch.
// 부수효과: @Scheduled로 3일마다 DB upsert + Caffeine 캐시("techTrends") 무효화.
@Slf4j
@Service
@RequiredArgsConstructor
public class TechTrendService {

    private final TechArticleRepository repository;

    private static final int ARTICLES_PER_SOURCE = 5;                          // 소스당 최신 글 수집 상한(피드 과다 적재 방지)
    private static final long REFRESH_INTERVAL_MS = 3L * 24 * 60 * 60 * 1000;  // 3일. 기술 블로그는 갱신이 잦지 않아 긴 주기로 둠
    private static final long INITIAL_DELAY_MS = 30_000L;                       // 30초. 기동 직후 부하/외부호출을 피해 살짝 지연 후 첫 수집
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");                  // 발행 시각을 한국 시간으로 변환해 저장
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");  // 화면 표시용 날짜 포맷
    // 본문/요약 HTML에서 첫 <img src="..."> 를 뽑아내는 정규식(대소문자 무시). 썸네일 fallback에 사용.
    private static final Pattern IMG_PATTERN =
        Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    // 키워드 기반 자동 태깅 규칙: {태그명, "쉼표로 나열한 소문자 키워드들"}.
    // 위에서부터 순서대로 검사하여 제목+요약에 키워드가 하나라도 포함되면 그 태그를 부여한다(우선순위 = 배열 순서).
    // 어느 규칙에도 안 걸리면 detectTag()가 기본값 "기술"을 돌려준다.
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

    // 목록 조회. 매 요청마다 DB를 때리지 않도록 Caffeine 캐시("techTrends")에 결과를 보관한다.
    // 캐시는 refreshArticles()가 새로 수집할 때 @CacheEvict로 통째로 비워져 갱신 직후 최신 글이 보이게 된다.
    @Cacheable("techTrends")
    @Transactional(readOnly = true)
    public List<TechArticleDto> getLatest() {
        return repository.findTop8ByOrderByPublishedAtDesc().stream()
            .map(this::toDto)
            .toList();
    }

    // 상세 단건 조회. 없는 id면 IllegalArgumentException → GlobalExceptionHandler에서 처리.
    @Transactional(readOnly = true)
    public TechArticleDto getById(UUID id) {
        TechArticle article = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("기사를 찾을 수 없습니다."));
        return toDto(article);
    }

    /* ─── 스케줄러 (3일마다 RSS 동기화) ─── */

    // 3일 주기 자동 수집 진입점. 기동 30초 후 첫 실행 → 이후 3일 간격(fixedRate)으로 반복.
    // 모든 RssSource를 순회하며 소스당 최신 N건을 가져와 upsert하고, 끝나면 목록 캐시를 전부 비운다.
    // @Transactional이라 한 번의 트랜잭션 안에서 dirty checking으로 update가 자동 반영된다.
    @Scheduled(fixedRate = REFRESH_INTERVAL_MS, initialDelay = INITIAL_DELAY_MS)
    @CacheEvict(value = "techTrends", allEntries = true)
    @Transactional
    public void refreshArticles() {
        log.info("Tech trends refresh started");
        int upserted = 0;
        for (RssSource source : RssSource.values()) {
            // 한 소스가 실패해도 fetchRss가 빈 리스트를 반환하므로 나머지 소스 수집은 계속 진행된다.
            for (TechArticle article : fetchRss(source)) {
                upsert(article);
                upserted++;
            }
        }
        log.info("Tech trends refresh completed: {} articles upserted", upserted);
    }

    // url 기준 upsert: 이미 있으면 변동 필드만 갱신(영속 상태라 트랜잭션 종료 시 자동 flush),
    // 없으면 새로 저장한다. url UNIQUE 제약과 짝을 이뤄 중복 적재를 막는다.
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

    // 단일 소스의 피드를 HTTP로 받아 ROME으로 파싱하고, 최신 N건을 엔티티로 변환해 반환.
    // 타임아웃을 짧게(connect 5s/read 10s) 둬 한 소스의 지연이 전체 스케줄을 묶지 않게 한다.
    // 어떤 예외든(네트워크/파싱 오류) 잡아서 WARN 로그만 남기고 빈 리스트를 반환 → 부분 실패 허용.
    private List<TechArticle> fetchRss(RssSource source) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(source.getUrl()).toURL().openConnection();
            // 일부 블로그가 기본 java User-Agent를 차단하므로 브라우저 흉내 UA를 명시한다.
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; JobIT/1.0)");
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(10_000);

            SyndFeedInput input = new SyndFeedInput();
            // try-with-resources로 스트림/리더를 확실히 닫는다. XmlReader가 피드 인코딩을 자동 감지.
            try (InputStream is = conn.getInputStream();
                 XmlReader reader = new XmlReader(is)) {
                SyndFeed feed = input.build(reader);
                return feed.getEntries().stream()
                    .limit(ARTICLES_PER_SOURCE)  // 소스당 상위 N건만 (피드는 보통 최신순 정렬)
                    .map(e -> toEntity(e, source))
                    .toList();
            }
        } catch (Exception e) {
            // 부분 실패 허용: 이 소스만 건너뛰고 전체 동기화는 계속된다.
            log.warn("RSS fetch failed [{}]: {}", source.getName(), e.getMessage());
            return List.of();
        }
    }

    // 피드 항목(SyndEntry) 하나를 TechArticle 엔티티로 정규화.
    // 본문/요약 추출, HTML 제거 후 요약 250자 컷, 발행시각 KST 변환, 썸네일 추출/절대경로화, 태그 분류까지 수행.
    private TechArticle toEntity(SyndEntry entry, RssSource source) {
        String content = extractContent(entry);
        // 요약(description)이 없으면 본문을 대신 요약 소스로 사용.
        String rawDesc = entry.getDescription() != null ? entry.getDescription().getValue() : content;
        String desc = stripHtml(rawDesc);
        if (desc.length() > 250) desc = desc.substring(0, 250) + "...";  // 카드 미리보기 길이 제한

        String title = entry.getTitle() != null ? entry.getTitle().trim() : "";
        // 발행일이 없는 항목은 수집 시각을 대신 사용(최신순 정렬에서 누락되지 않도록).
        LocalDateTime publishedAt = entry.getPublishedDate() != null
            ? LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), KST)
            : LocalDateTime.now();

        String imageUrl = extractImageUrl(entry, content, rawDesc);
        // 상대경로(//, /path)로 온 이미지 주소를 원문 링크 기준 절대 URL로 보정.
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

    // 본문 추출 우선순위: content:encoded 등 contents의 첫 비어있지 않은 값 → 없으면 description → 둘 다 없으면 빈 문자열.
    // 피드마다 본문을 담는 위치가 달라(RSS content vs description) 폴백 체인을 둔다.
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

    // 썸네일 URL 추출. 피드별로 이미지 제공 방식이 제각각이라 3단계 폴백으로 찾는다.
    private String extractImageUrl(SyndEntry entry, String content, String description) {
        // 1. Media RSS 모듈(media:thumbnail → media:content) 우선. 가장 신뢰도 높은 대표 이미지.
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

        // 2. enclosure 중 type이 image/* 인 첨부. RSS 표준 첨부 방식.
        if (entry.getEnclosures() != null) {
            for (SyndEnclosure enc : entry.getEnclosures()) {
                if (enc.getType() != null && enc.getType().startsWith("image/") && enc.getUrl() != null) {
                    return enc.getUrl();
                }
            }
        }

        // 3. 최후 폴백: 본문/요약 HTML을 합쳐 정규식으로 첫 <img src> 추출.
        String html = (content != null ? content : "") + " " + (description != null ? description : "");
        Matcher m = IMG_PATTERN.matcher(html);
        if (m.find()) return m.group(1);

        return null;  // 어떤 방식으로도 못 찾으면 썸네일 없음
    }

    // 이미지 주소를 절대 URL로 정규화. 프로토콜 상대(//), 절대(http/https), 상대경로 케이스를 각각 처리.
    // 상대경로는 원문 글 URL(articleUrl)을 base로 resolve 한다. 변환 실패 시 null로 폴백(썸네일 생략).
    private String resolveUrl(String src, String articleUrl) {
        if (src == null || src.isBlank()) return null;
        if (src.startsWith("//")) return "https:" + src;  // 프로토콜 상대 → https로 고정
        if (src.startsWith("http://") || src.startsWith("https://")) return src;  // 이미 절대 URL
        if (articleUrl != null) {
            try {
                return URI.create(articleUrl).resolve(src).toString();  // 상대경로를 글 URL 기준으로 절대화
            } catch (Exception ignore) {
                return null;
            }
        }
        return null;
    }

    /* ─── 변환 / 유틸 ─── */

    // 엔티티 → 응답 DTO 변환. publishedAt은 화면 표시용 "yyyy.MM.dd" 문자열로 포맷(없으면 빈 문자열).
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

    // 요약 표시용으로 HTML을 평문화: 태그 제거 → 자주 쓰는 엔티티(&amp; 등) 복원/공백화 → 연속 공백 정리.
    // 본문(content)이 아니라 카드 미리보기(description) 생성에만 쓰인다(상세는 원본 HTML 유지).
    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", " ")
                   .replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                   .replaceAll("&gt;", ">").replaceAll("&nbsp;|&#[0-9]+;", " ")
                   .replaceAll("\\s+", " ").trim();
    }

    // 제목+요약을 소문자로 합쳐 TAG_RULES를 위에서부터 검사, 첫 매칭 키워드의 태그를 반환.
    // 규칙 순서가 곧 우선순위이며, 아무것도 안 걸리면 기본 태그 "기술".
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