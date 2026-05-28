package com.main.jobit.domain.study;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyJoinRequestRepository extends JpaRepository<StudyJoinRequest, UUID> {

    /** 중복 신청 차단용. */
    boolean existsByStudyIdAndApplicantId(UUID studyId, UUID applicantId);

    Optional<StudyJoinRequest> findByStudyIdAndApplicantId(UUID studyId, UUID applicantId);

    /** 정원 카운트(ACCEPTED) 또는 PENDING 통계 단건 조회. */
    long countByStudyIdAndStatus(UUID studyId, JoinRequestStatus status);

    /** 작성자 화면용 — 한 스터디의 신청자 전체 목록 (최신순). */
    List<StudyJoinRequest> findByStudyIdOrderByCreatedAtDesc(UUID studyId);

    /**
     * 목록 화면용 — N개 스터디의 ACCEPTED 신청 수를 한 번의 GROUP BY로 조회.
     * 카드마다 개별 COUNT 호출하는 N+1을 방지.
     */
    @Query("""
            SELECT r.study.id AS studyId, COUNT(r) AS cnt
              FROM StudyJoinRequest r
             WHERE r.study.id IN :studyIds AND r.status = 'ACCEPTED'
             GROUP BY r.study.id
            """)
    List<AcceptedCount> countAcceptedByStudyIds(@Param("studyIds") Collection<UUID> studyIds);

    /** {@link #countAcceptedByStudyIds} 결과용 projection. */
    interface AcceptedCount {
        UUID getStudyId();
        long getCnt();
    }
}
