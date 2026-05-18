package com.main.jobit.domain.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory, UUID> {
    Optional<QuestionCategory> findByName(String name);
}