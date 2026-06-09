package com.main.jobit.domain.interview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// 면접 질문(턴) 영속성. 단건 평가 갱신 외에, 답변 제출은 race condition을 막으려고 파생 메서드가 아닌
// 조건부 단일 UPDATE(submitAnswerIfEligible)로 처리한다.
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, UUID> {

    // 세션 화면 표시·종합 피드백 구성용. orderNo 오름차순(자기소개 1번 → AI 질문 순)으로 정렬해 반환.
    List<InterviewQuestion> findBySessionIdOrderByOrderNoAsc(UUID sessionId);

    /**
     * 답변 제출을 원자적으로 처리. WAITING/EVAL_FAILED 상태일 때만 transcript를 저장하고 EVAL_PENDING으로 전이.
     * 동시 중복 제출 시 두 번째 호출은 affected rows 0으로 차단된다.
     */
    // @Modifying: 벌크 UPDATE임을 명시. clear/flush로 영속성 컨텍스트와 DB 상태가 어긋나지 않게 동기화.
    // 반환값(affected rows)을 보고 호출 측이 성공/충돌을 판단하므로 트랜잭션 안에서 실행해야 한다.
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    // WHERE의 상태 조건이 곧 '제출 가능 상태' 정의(WAITING 또는 EVAL_FAILED). 이미 EVAL_PENDING/EVAL_DONE이면
    // 매칭되는 행이 없어 0건이 업데이트된다 → 동시/중복 제출 중 두 번째 요청을 DB 레벨에서 막는 낙관적 가드.
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
    // 반환값: 갱신된 행 수. 1이면 제출 성공, 0이면 이미 답변/평가 중이라 차단됨(서비스가 409로 변환).
    int submitAnswerIfEligible(@Param("id") UUID id,
                               @Param("transcript") String transcript,
                               @Param("answeredAt") LocalDateTime answeredAt);
}
