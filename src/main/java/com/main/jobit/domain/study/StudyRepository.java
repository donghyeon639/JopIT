package com.main.jobit.domain.study;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StudyRepository extends JpaRepository<Study, UUID>, JpaSpecificationExecutor<Study> {

    /** 상세 조회 시 조회수 +1 (race-safe). 후속 findById가 최신 값을 보도록 영속성 컨텍스트 비움. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Study s SET s.viewCount = s.viewCount + 1 WHERE s.id = :id")
    int incrementViewCount(@Param("id") UUID id);

    /** 이번주 인기 모집글 (조회수 기준 상위 6건). 추후 기간 필터 도입 시 시그니처 변경 가능. */
    List<Study> findTop6ByOrderByViewCountDesc();
}
