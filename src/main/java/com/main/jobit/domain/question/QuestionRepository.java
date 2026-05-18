package com.main.jobit.domain.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findByQuestionCategoryId(UUID questionCategoryId, Pageable pageable);

    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findByDifficulty(Difficulty difficulty, Pageable pageable);

    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findByQuestionCategoryIdAndDifficulty(UUID questionCategoryId, Difficulty difficulty, Pageable pageable);

}