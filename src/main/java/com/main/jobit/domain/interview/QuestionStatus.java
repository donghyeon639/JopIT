package com.main.jobit.domain.interview;

/**
 * 면접 질문(=한 턴) 상태.
 * WAITING(미답변) → (답변 제출) → EVAL_PENDING → (LLM 평가) → EVAL_DONE
 * 평가 실패 시 EVAL_FAILED (재제출 가능).
 */
public enum QuestionStatus {
    WAITING,
    EVAL_PENDING,
    EVAL_DONE,
    EVAL_FAILED
}
