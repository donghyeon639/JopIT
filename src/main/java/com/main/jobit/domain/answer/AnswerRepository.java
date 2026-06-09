package com.main.jobit.domain.answer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

// Answer 엔티티에 대한 영속성 계층. 기본 CRUD 외에
// 질문별/사용자별/전체 최신순 조회와 피드백 상태 조건부 갱신 쿼리를 제공한다.
public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    // 특정 질문에 달린 답변 목록(최신순). 질문 상세 화면에서 사용.
    List<Answer> findByQuestionIdOrderByCreatedAtDesc(UUID questionId);

    // 특정 사용자가 작성한 답변 목록(최신순). 마이페이지 "내 답변" 조회에 사용.
    List<Answer> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // 전체 답변 최신순. 커뮤니티 피드에 사용.
    List<Answer> findAllByOrderByCreatedAtDesc();

    // 피드백 요청을 "원자적"으로 받기 위한 조건부 UPDATE.
    // NONE/FAILED 상태일 때만 PENDING 으로 바꾸므로, 이미 PENDING/DONE 인 답변은 건드리지 않는다.
    // → 동시에 들어온 중복 요청을 DB 차원에서 막아(영향 행 0 이면 거절) 이중 피드백 생성을 방지한다.
    // clearAutomatically/flushAutomatically = true: 벌크 UPDATE 후 영속성 컨텍스트를 동기화해
    // 직후 같은 트랜잭션에서 엔티티를 다시 읽을 때 옛 상태(stale)를 보지 않도록 한다.
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
    int markFeedbackPendingIfEligible(@Param("id") UUID id); // 반환값: 갱신된 행 수(1 이면 요청 수락, 0 이면 거절)
}
