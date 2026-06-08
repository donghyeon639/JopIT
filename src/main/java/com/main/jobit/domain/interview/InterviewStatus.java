package com.main.jobit.domain.interview;

/**
 * 면접 세션 상태.
 * QUESTIONS_PENDING → (LLM 질문 생성) → QUESTIONS_READY → (답변/평가) → COMPLETED
 * 질문 생성 실패 시 QUESTIONS_FAILED.
 */
public enum InterviewStatus {
    QUESTIONS_PENDING,
    QUESTIONS_READY,
    QUESTIONS_FAILED,
    COMPLETED
}
