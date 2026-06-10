package com.main.jobit.domain.interview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

// 면접 세션 영속성. 단건 조회는 기본 메서드로 충분하고, 목록 조회만 N+1 회피용 커스텀 쿼리를 둔다.
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID> {

    // 기록 목록 조회 — 응답에 jobCategory.name이 필요한데 연관이 LAZY라 세션마다 추가 쿼리가 나간다.
    // fetch join으로 한 번에 끌어와 N+1을 막고, 최신순(createdAt desc)으로 정렬해 반환한다.
    @Query("""
            select s from InterviewSession s
            join fetch s.jobCategory
            where s.user.id = :userId
            order by s.createdAt desc
            """)
    List<InterviewSession> findByUserIdWithJobCategory(@Param("userId") UUID userId);
}
