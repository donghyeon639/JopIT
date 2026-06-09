package com.main.jobit.domain.answer;

// 답변에 대한 AI 피드백 진행 상태를 나타내는 enum.
// 비동기 처리 라이프사이클을 표현하며, 정상 흐름은 NONE → PENDING → DONE,
// 실패 시 NONE/PENDING → FAILED 로 전이된다. 재요청은 NONE/FAILED 상태에서만 허용된다.
public enum FeedbackStatus {
    NONE,    // 아직 피드백을 요청한 적 없음(초기 상태)
    PENDING, // 피드백 요청 접수 후 비동기 처리 대기/진행 중
    DONE,    // 피드백 생성 완료(aiFeedback 채워짐)
    FAILED   // 피드백 생성 실패 — 재요청 가능
}