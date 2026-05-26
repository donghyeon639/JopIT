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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(name = "picked_by_admin_id", columnDefinition = "uuid")
    private UUID pickedByAdminId;

    @Column(length = 200)
    private String reason;

    @Column(name = "featured_until", nullable = false)
    private LocalDateTime featuredUntil;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public FeaturedJob(JobPosting jobPosting, UUID pickedByAdminId,
                       String reason, LocalDateTime featuredUntil, int displayOrder) {
        this.jobPosting = jobPosting;
        this.pickedByAdminId = pickedByAdminId;
        this.reason = reason;
        this.featuredUntil = featuredUntil;
        this.displayOrder = displayOrder;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void update(String reason, LocalDateTime featuredUntil, int displayOrder) {
        this.reason = reason;
        this.featuredUntil = featuredUntil;
        this.displayOrder = displayOrder;
    }
}