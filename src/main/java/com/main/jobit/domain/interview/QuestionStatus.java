package com.main.jobit.domain.interview;

/**
 * 면접 질문(=한 턴) 상태.
 * WAITING(미답변) → (답변 제출) → EVAL_PENDING → (LLM 평가) → EVAL_DONE
 * 평가 실패 시 EVAL_FAILED (재제출 가능).
 */
// 질문(턴) 단위 상태 머신. 답변 제출/평가의 원자적 전이 기준이 된다.
// 답변 제출 가능 상태는 WAITING 또는 EVAL_FAILED 둘뿐이며, 이 조건은 submitAnswerIfEligible의 WHERE 절과 일치해야 한다.
public enum QuestionStatus {
    WAITING,      // 아직 답변 전. 질문 생성 직후 기본값.
    EVAL_PENDING, // 답변 제출됨(transcript 저장). 비동기 LLM 평가 대기/진행 중 — 중복 제출을 막는 잠금 역할도 한다.
    EVAL_DONE,    // 평가 완료. evaluation 텍스트가 채워진 최종 상태.
    EVAL_FAILED   // 평가 LLM 호출 실패. WAITING과 함께 '재제출 허용' 상태로 취급해 사용자가 다시 답할 수 있다.
}
