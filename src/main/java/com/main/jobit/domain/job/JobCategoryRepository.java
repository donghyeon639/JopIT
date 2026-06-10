package com.main.jobit.domain.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// JobCategory(직군) 영속성 접근 레포지토리. 기본 CRUD는 JpaRepository가 제공.
public interface JobCategoryRepository extends JpaRepository<JobCategory, UUID> {
    // 직군명으로 조회. 중복 직군명 검사 등에 사용(없으면 Optional.empty).
    Optional<JobCategory> findByName(String name);
}

