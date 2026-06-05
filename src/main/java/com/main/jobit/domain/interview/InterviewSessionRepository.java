package com.main.jobit.domain.interview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID> {

    /** 기록 목록 조회 — jobCategory를 fetch join으로 함께 로드해 N+1을 막는다. */
    @Query("""
            select s from InterviewSession s
            join fetch s.jobCategory
            where s.user.id = :userId
            order by s.createdAt desc
            """)
    List<InterviewSession> findByUserIdWithJobCategory(@Param("userId") UUID userId);
}
