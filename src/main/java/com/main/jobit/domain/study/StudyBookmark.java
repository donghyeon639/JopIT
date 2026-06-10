package com.main.jobit.domain.study;

import com.main.jobit.domain.user.Users;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자가 스터디 모집글을 북마크(찜)한 기록. (user, study) 한 쌍당 한 행.
 *
 * 유니크 제약(uk_bookmarks_user_study): 같은 사용자가 같은 스터디를 중복 북마크하는 것을 DB에서 차단.
 *   → 토글 로직이 "있으면 삭제, 없으면 추가"로 단순해질 수 있는 근거.
 * 인덱스(user_id,created_at): 내 북마크 목록을 최신순으로 페이징 조회하기 위한 복합 인덱스.
 */
@Entity
@Table(
        name = "study_bookmarks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bookmarks_user_study",
                columnNames = {"user_id", "study_id"}),
        indexes = @Index(name = "idx_bookmarks_user_created", columnList = "user_id,created_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 북마크한 사용자. LAZY — 목록 조회 시 사용자 엔티티 전체를 즉시 로딩할 필요 없음.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 북마크 대상 스터디.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;   // 북마크한 시각. 내 북마크 목록 정렬 기준.

    @Builder
    public StudyBookmark(Users user, Study study) {
        this.user = user;
        this.study = study;
    }

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
