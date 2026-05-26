package com.main.jobit.domain.jobposting;

import com.main.jobit.domain.job.JobCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(
        name = "job_posting",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_job_posting_source_external",
                columnNames = {"source", "external_id"}
        ),
        indexes = {
                @Index(name = "idx_job_posting_category_posted",
                        columnList = "job_category_id, posted_at"),
                @Index(name = "idx_job_posting_expires", columnList = "expires_at"),
                @Index(name = "idx_job_posting_active_posted",
                        columnList = "is_active, posted_at"),
                @Index(name = "idx_job_posting_source_active_posted",
                        columnList = "source, is_active, posted_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobSource source;

    @Column(name = "external_id", nullable = false, length = 200)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_category_id")
    private JobCategory jobCategory;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 200)
    private String company;

    @Column(length = 200)
    private String location;

    @Column(name = "career_level", length = 50)
    private String careerLevel;

    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Column(name = "salary_range", length = 200)
    private String salaryRange;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "apply_url", columnDefinition = "TEXT")
    private String applyUrl;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Builder
    public JobPosting(JobSource source, String externalId, JobCategory jobCategory,
                      String title, String company, String location,
                      String careerLevel, String employmentType, String salaryRange,
                      LocalDateTime postedAt, LocalDateTime expiresAt,
                      String applyUrl, String rawPayload) {
        this.source = source;
        this.externalId = externalId;
        this.jobCategory = jobCategory;
        this.title = title;
        this.company = company;
        this.location = location;
        this.careerLevel = careerLevel;
        this.employmentType = employmentType;
        this.salaryRange = salaryRange;
        this.postedAt = postedAt;
        this.expiresAt = expiresAt;
        this.applyUrl = applyUrl;
        this.rawPayload = rawPayload;
        this.active = true;
    }

    @PrePersist
    void onCreate() {
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
    }

    public void update(JobCategory jobCategory, String title, String company, String location,
                       String careerLevel, String employmentType, String salaryRange,
                       LocalDateTime postedAt, LocalDateTime expiresAt,
                       String applyUrl, String rawPayload) {
        this.jobCategory = jobCategory;
        this.title = title;
        this.company = company;
        this.location = location;
        this.careerLevel = careerLevel;
        this.employmentType = employmentType;
        this.salaryRange = salaryRange;
        this.postedAt = postedAt;
        this.expiresAt = expiresAt;
        this.applyUrl = applyUrl;
        this.rawPayload = rawPayload;
        this.fetchedAt = LocalDateTime.now();
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}