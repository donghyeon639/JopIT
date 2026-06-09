package com.main.jobit.domain.study;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 참여 신청 영속성. 중복 신청 차단, 정원 카운트, 신청자 목록, 목록 화면용 일괄 집계를 제공.
public interface StudyJoinRequestRepository extends JpaRepository<StudyJoinRequest, UUID> {

    /** 중복 신청 차단용. 신청 전 사전 검사(최종 차단은 DB 유니크 제약). */
    boolean existsByStudyIdAndApplicantId(UUID studyId, UUID applicantId);

    // (study, applicant) 단건 신청 조회. 재신청/조회 시 기존 신청을 찾을 때 사용.
    Optional<StudyJoinRequest> findByStudyIdAndApplicantId(UUID studyId, UUID applicantId);

    /** 정원 카운트(ACCEPTED) 또는 PENDING 통계 단건 조회. 정원 초과 검사·상세 applied 계산에 사용. */
    long countByStudyIdAndStatus(UUID studyId, JoinRequestStatus status);

    /** 작성자 화면용 — 한 스터디의 신청자 전체 목록 (최신순). applicant LAZY 페치를 JOIN FETCH로 해소. */
    @Query("""
            SELECT r FROM StudyJoinRequest r
              JOIN FETCH r.applicant
             WHERE r.study.id = :studyId
             ORDER BY r.createdAt DESC
            """)
    List<StudyJoinRequest> findByStudyIdWithApplicant(@Param("studyId") UUID studyId);

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
