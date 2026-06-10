package com.main.jobit.domain.study;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

// 스터디 영속성. JpaSpecificationExecutor를 함께 상속해 StudySpecifications 기반 동적 목록 조회를 지원.
public interface StudyRepository extends JpaRepository<Study, UUID>, JpaSpecificationExecutor<Study> {

    /**
     * 상세 조회 시 조회수 +1 (race-safe).
     * 영속 엔티티를 읽어 +1 후 저장하면 동시 요청 간 lost update가 생기므로, DB에서 직접 산술 UPDATE한다.
     * clearAutomatically=true : UPDATE 후 영속성 컨텍스트를 비워, 같은 트랜잭션의 후속 findById가 증가된 최신 값을 읽도록.
     * flushAutomatically=true  : 보류 중인 변경을 UPDATE 전에 먼저 flush.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Study s SET s.viewCount = s.viewCount + 1 WHERE s.id = :id")
    int incrementViewCount(@Param("id") UUID id);

    /** 이번주 인기 모집글 (조회수 기준 상위 6건). 추후 기간 필터 도입 시 시그니처 변경 가능. */
    List<Study> findTop6ByOrderByViewCountDesc();
}
