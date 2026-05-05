package com.main.jobit.answer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    List<Answer> findByQuestionIdOrderByCreatedAtDesc(UUID questionId);

    List<Answer> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Answer> findAllByOrderByCreatedAtDesc();
}