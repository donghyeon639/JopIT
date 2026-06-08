package com.main.jobit.domain.interview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, UUID> {

    List<InterviewQuestion> findBySessionIdOrderByOrderNoAsc(UUID sessionId);

    /**
     * 답변 제출을 원자적으로 처리. WAITING/EVAL_FAILED 상태일 때만 transcript를 저장하고 EVAL_PENDING으로 전이.
     * 동시 중복 제출 시 두 번째 호출은 affected rows 0으로 차단된다.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update InterviewQuestion q
               set q.status = com.main.jobit.domain.interview.QuestionStatus.EVAL_PENDING,
                   q.transcript = :transcript,
                   q.answeredAt = :answeredAt
             where q.id = :id
               and q.status in (
                   com.main.jobit.domain.interview.QuestionStatus.WAITING,
                   com.main.jobit.domain.interview.QuestionStatus.EVAL_FAILED
               )
            """)
    int submitAnswerIfEligible(@Param("id") UUID id,
                               @Param("transcript") String transcript,
                               @Param("answeredAt") LocalDateTime answeredAt);
}
