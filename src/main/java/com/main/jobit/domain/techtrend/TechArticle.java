package com.main.jobit.domain.techtrend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// RSS/Atom 피드에서 수집한 기술 블로그 글 한 건을 저장하는 엔티티.
// url에 UNIQUE 제약을 두어, 동일 글이 재수집되면 새 행을 추가하지 않고 기존 행을 갱신(upsert)하는 기준 키로 쓴다.
// (TechTrendService.upsert()가 findByUrl로 중복을 판단한다.)
@Entity
@Table(name = "tech_article",
       uniqueConstraints = @UniqueConstraint(name = "uk_tech_article_url", columnNames = "url"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 전용 기본 생성자. 외부에서 무인자 생성 막기 위해 PROTECTED.
public class TechArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 원문 링크이자 중복 판별 키(UNIQUE). 길이가 긴 URL을 고려해 1000자로 잡음.
    @Column(nullable = false, length = 1000)
    private String url;

    @Column(nullable = false, length = 500)
    private String title;

    // 출처 표시 이름(RssSource.name 값). enum 대신 문자열로 저장해 소스 enum 변경에 덜 민감하게 둠.
    @Column(nullable = false, length = 100)
    private String source;

    // 카드 미리보기용 요약. HTML 제거 + 250자로 잘라 저장한다(toEntity 참고).
    @Column(length = 500)
    private String description;

    // 상세 페이지용 본문(피드가 주는 full HTML 그대로). 길이 제한 없이 TEXT로 보관.
    @Column(columnDefinition = "TEXT")
    private String content;

    // 키워드 규칙으로 자동 분류한 태그(AI/ML, Java 등). detectTag()가 채운다.
    @Column(length = 50)
    private String tag;

    // 카드 썸네일. media:thumbnail / enclosure / 본문 첫 <img> 순으로 추출(extractImageUrl).
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    // 원문 발행 시각(KST 기준으로 변환 저장). 최신순 정렬·노출 기준.
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // 우리 시스템이 마지막으로 수집/갱신한 시각. 신선도 추적용.
    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    // 신규 생성 전용 빌더. id/fetchedAt/active 등은 직접 받지 않고 생성·@PrePersist에서 채운다.
    @Builder
    public TechArticle(String url, String title, String source, String description,
                       String content, String tag, String imageUrl, LocalDateTime publishedAt) {
        this.url = url;
        this.title = title;
        this.source = source;
        this.description = description;
        this.content = content;
        this.tag = tag;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
    }

    // 영속화 직전 훅: 빌더에서 fetchedAt을 받지 않으므로 여기서 수집 시각을 채운다.
    @PrePersist
    void onCreate() {
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
    }

    // 재수집 시 동일 url의 기존 행을 갱신(upsert)하는 메서드.
    // url/source는 식별 키라 바뀌지 않으므로 인자에서 제외하고, 변동 가능한 필드만 덮어쓴다.
    // 갱신할 때마다 fetchedAt을 현재 시각으로 새로 찍어 신선도를 갱신한다.
    public void update(String title, String description, String content,
                       String tag, String imageUrl, LocalDateTime publishedAt) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.tag = tag;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
        this.fetchedAt = LocalDateTime.now();
    }
}
