package com.main.jobit.domain.answer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    List<Answer> findByQuestionIdOrderByCreatedAtDesc(UUID questionId);

    List<Answer> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Answer> findAllByOrderByCreatedAtDesc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Answer a
               set a.feedbackStatus = com.main.jobit.domain.answer.FeedbackStatus.PENDING
             where a.id = :id
               and a.feedbackStatus in (
                   com.main.jobit.domain.answer.FeedbackStatus.NONE,
                   com.main.jobit.domain.answer.FeedbackStatus.FAILED
               )
            """)
    int markFeedbackPendingIfEligible(@Param("id") UUID id);
}
