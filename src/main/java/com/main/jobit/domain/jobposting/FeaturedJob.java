package com.main.jobit.domain.jobposting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// 관리자가 특정 채용 공고를 "추천(featured)"으로 큐레이션하기 위한 엔티티.
// 자동 동기화되는 JobPosting과 분리해, 노출 기간(featuredUntil)과 정렬 순서(displayOrder)를 별도로 관리한다.
// 복합 인덱스(featured_until, display_order)는 "유효 기간 내 추천을 표시 순서로 조회"하는 질의를 빠르게 하기 위함.
@Entity
@Table(
        name = "featured_job",
        indexes = {
                @Index(name = "idx_featured_job_until_order",
                        columnList = "featured_until, display_order")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeaturedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 추천 대상 공고. 지연 로딩(LAZY)이며 반드시 존재해야 함(optional=false).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    // 추천을 등록한 관리자 id(감사/추적용). FK 매핑 대신 단순 UUID로 보관.
    @Column(name = "picked_by_admin_id", columnDefinition = "uuid")
    private UUID pickedByAdminId;

    private String reason;  // 추천 사유 메모(선택)

    // 노출 만료 시각. 이 시각 이후에는 추천 목록에서 제외하는 식으로 기간 한정 노출.
    @Column(name = "featured_until", nullable = false)
    private LocalDateTime featuredUntil;

    // 추천 목록 내 정렬 우선순위(작을수록 먼저).
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    // 생성 시각. updatable=false로 최초 1회만 기록되고 이후 변경되지 않음(@PrePersist에서 세팅).
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 추천 신규 등록용 빌더. createdAt은 영속 직전 자동 세팅하므로 인자에서 제외.
    @Builder
    public FeaturedJob(JobPosting jobPosting, UUID pickedByAdminId,
                       String reason, LocalDateTime featuredUntil, int displayOrder) {
        this.jobPosting = jobPosting;
        this.pickedByAdminId = pickedByAdminId;
        this.reason = reason;
        this.featuredUntil = featuredUntil;
        this.displayOrder = displayOrder;
    }

    // 영속화 직전 생성 시각 기록.
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // 추천 메타데이터(사유/만료/정렬) 수정용. 대상 공고(jobPosting)는 바꾸지 않는다.
    public void update(String reason, LocalDateTime featuredUntil, int displayOrder) {
        this.reason = reason;
        this.featuredUntil = featuredUntil;
        this.displayOrder = displayOrder;
    }
}