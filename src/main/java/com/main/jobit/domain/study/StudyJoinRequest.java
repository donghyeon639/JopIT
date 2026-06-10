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
 *
 * 유니크 제약(study_id, applicant_id): 같은 사용자가 같은 스터디에 중복 신청하는 것을 DB에서 차단.
 *   → 서비스의 existsBy... 사전 검사가 경쟁 상태로 뚫리더라도 최후의 안전망이 됨.
 * 인덱스:
 *   - study_id,status : 작성자 화면의 신청자 목록 + ACCEPTED 정원 카운트 조회 최적화
 *   - applicant_id    : "내가 신청한 스터디" 조회용
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

    // 신청 대상 스터디.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    // 신청자(지원자). 작성자 화면에서 닉네임을 보여주므로 목록 조회 시 JOIN FETCH로 함께 로딩한다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Users applicant;

    // 신청 상태. PENDING → ACCEPTED/REJECTED 단방향 전이(JoinRequestStatus 참고).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoinRequestStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;   // 신청 시 남기는 자기소개/한 마디. 선택 입력이라 nullable.

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")   // 수락/거절로 상태가 바뀐 시각. 최초 신청 시에는 null.
    private LocalDateTime updatedAt;

    // 신청 생성 시 status를 항상 PENDING으로 고정 — 외부에서 ACCEPTED 상태로 바로 만들 수 없게 한다.
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

    // 상태 전이 메서드. 가드(PENDING인지 등)는 Service의 decide()에서 수행하므로 여기서는 단순 대입만 한다.
    public void accept() {
        this.status = JoinRequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = JoinRequestStatus.REJECTED;
    }

    // 아직 미처리(대기) 상태인지. 이미 처리된 신청을 재처리하지 못하도록 decide()에서 검사.
    public boolean isPending() {
        return this.status == JoinRequestStatus.PENDING;
    }
}
