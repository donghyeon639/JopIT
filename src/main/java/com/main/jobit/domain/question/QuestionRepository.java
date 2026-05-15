package com.main.jobit.domain.question;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByQuestionCategoryId(UUID questionCategoryId);

    List<Question> findByDifficulty(Difficulty difficulty);

    List<Question> findByQuestionCategoryIdAndDifficulty(UUID questionCategoryId, Difficulty difficulty);
}