package com.main.jobit.domain.interview;

/**
 * 면접 세션 상태.
 * QUESTIONS_PENDING → (LLM 질문 생성) → QUESTIONS_READY → (답변/평가) → COMPLETED
 * 질문 생성 실패 시 QUESTIONS_FAILED.
 */
// 세션 단위 상태 머신. 비동기 질문 생성·종합 피드백 생성의 진행도를 프런트가 폴링으로 추적하는 근거가 된다.
public enum InterviewStatus {
    QUESTIONS_PENDING, // 세션 생성 직후 기본값. 비동기 LLM이 아직 질문을 만들고 있는 중 — 이 상태에선 답변 제출 불가.
    QUESTIONS_READY,   // 질문 생성 완료. 사용자가 답변을 제출할 수 있는 정상 진행 상태.
    QUESTIONS_FAILED,  // LLM 호출/파싱/저장 중 실패. PENDING 영구 고착을 막으려고 명시적 종료 상태로 전이시킨다.
    COMPLETED          // 면접 종료 후 종합 피드백까지 반영된 최종 상태.
}
