package com.main.jobit.domain.study;

import com.main.jobit.domain.user.Users;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 스터디 참여 신청. 한 사용자가 한 스터디에 신청하면 한 행이 만들어진다.
 * 작성자가 {@link JoinRequestStatus#ACCEPTED} 또는 {@link JoinRequestStatus#REJECTED}로 변경한다.
 */
@Entity
@Table(
        name = "study_join_requests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_study_join_requests_study_applicant",
                columnNames = {"study_id", "applicant_id"}),
        indexes = {
                @Index(name = "idx_join_requests_study_status", columnList = "study_id,status"),
                @Index(name = "idx_join_requests_applicant", columnList = "applicant_id"),
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Users applicant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoinRequestStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public StudyJoinRequest(Study study, Users applicant, String message) {
        this.study = study;
        this.applicant = applicant;
        this.message = message;
        this.status = JoinRequestStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = JoinRequestStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void accept() {
        this.status = JoinRequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = JoinRequestStatus.REJECTED;
    }

    public boolean isPending() {
        return this.status == JoinRequestStatus.PENDING;
    }
}
