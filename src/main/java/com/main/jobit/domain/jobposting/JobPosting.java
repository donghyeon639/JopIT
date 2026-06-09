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

// 외부 채용 소스에서 동기화한 채용 공고 한 건을 저장하는 엔티티.
// (source, external_id) 복합 UNIQUE로 동일 공고 중복 적재를 막고, 이를 upsert 기준 키로 사용한다.
// 인덱스들은 화면 질의 패턴(노출 중 최신순, 소스별 최신순, 카테고리별, 마감 임박 등)을 커버하기 위함.
@Entity
@Table(
        name = "job_posting",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_job_posting_source_external",
                columnNames = {"source", "external_id"}  // 출처+원본ID 조합으로 유일성 보장(중복 방지 키)
        ),
        indexes = {
                // 카테고리별 최신 공고 조회용
                @Index(name = "idx_job_posting_category_posted",
                        columnList = "job_category_id, posted_at"),
                // 마감일 기반 조회/정리(만료 처리)용
                @Index(name = "idx_job_posting_expires", columnList = "expires_at"),
                // 공개 목록 기본 질의(노출 중 + 최신순) 커버 인덱스
                @Index(name = "idx_job_posting_active_posted",
                        columnList = "is_active, posted_at"),
                // 소스 필터 + 노출 중 + 최신순 질의 커버 인덱스
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

    // 출처. EnumType.STRING으로 저장해 enum 순서가 바뀌어도 데이터가 깨지지 않게 한다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobSource source;

    // 원본 소스 내 공고 식별자. source와 묶여 중복 판별 키가 된다.
    @Column(name = "external_id", nullable = false, length = 200)
    private String externalId;

    // 직군 분류(선택). 동기화 시점엔 비어있을 수 있고, update에서도 기존 값을 유지한다(분류는 별도 관리).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_category_id")
    private JobCategory jobCategory;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 200)
    private String company;

    private String location;       // 근무지

    @Column(name = "career_level", length = 50)
    private String careerLevel;    // 경력 구분(신입/경력 등)

    @Column(name = "employment_type", length = 50)
    private String employmentType; // 고용 형태(정규/계약 등)

    @Column(name = "salary_range", length = 200)
    private String salaryRange;    // 급여 범위

    @Column(name = "posted_at")
    private LocalDateTime postedAt;   // 게시일(목록 정렬 기준)

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;  // 마감일

    @Column(name = "apply_url", columnDefinition = "TEXT")
    private String applyUrl;       // 지원 링크(길 수 있어 TEXT)

    // 외부 API 원문 payload 보관용(디버깅/재처리 대비). 동기화 update 시에는 null로 넘겨 굳이 덮어쓰지 않는다.
    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    // 우리 시스템이 마지막으로 동기화한 시각. 갱신 때마다 새로 찍힌다.
    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    // 노출 여부. 동기화로 다시 잡히면 true로 되살아나고, deactivate()로 숨길 수 있다(소프트 삭제).
    @Column(name = "is_active", nullable = false)
    private boolean active;

    // 신규 동기화 적재용 빌더. active는 생성 시 true 고정, fetchedAt은 @PrePersist에서 채움.
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

    // 영속화 직전 동기화 시각 기록.
    @PrePersist
    void onCreate() {
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
    }

    // 재동기화 시 기존 공고 갱신용. source/externalId(식별 키)는 불변이라 인자에서 제외.
    // 다시 잡힌 공고는 active=true로 되살리고 fetchedAt을 갱신해 신선도를 최신화한다.
    // (호출부 JobPostingSyncService는 jobCategory에 기존 값을, rawPayload에 null을 넘겨 분류/원문을 유지한다.)
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

    // 소프트 삭제: 행을 지우지 않고 노출만 끈다(마감/내림 처리 등).
    public void deactivate() {
        this.active = false;
    }
}