package com.main.jobit.domain.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// QuestionCategory(문제 카테고리) 영속성 레포지토리.
public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory, UUID> {
    // 카테고리명으로 조회 — 생성/수정 시 중복명 검사에 사용.
    Optional<QuestionCategory> findByName(String name);
}