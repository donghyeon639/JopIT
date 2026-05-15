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

@Entity
@Table(name = "tech_article",
       uniqueConstraints = @UniqueConstraint(name = "uk_tech_article_url", columnNames = "url"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String tag;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

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

    @PrePersist
    void onCreate() {
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
    }

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
