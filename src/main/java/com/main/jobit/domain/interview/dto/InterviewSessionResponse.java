package com.main.jobit.domain.interview.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// 세션 상세 응답. 생성/제출/종료/조회 모든 엔드포인트가 이 단일 타입으로 현재 상태를 돌려주며,
// 프런트는 이 응답(특히 status와 questions 안의 status)을 폴링해 비동기 진행도를 추적한다.
public record InterviewSessionResponse(
        UUID id,
        String jobCategory,                       // 직군명
        String interviewType,                     // InterviewType.name() — 코드/분기용 식별자
        String interviewTypeLabel,                // InterviewType.label() — 화면 표기용 한글 라벨
        String status,                            // InterviewStatus.name() 문자열
        String overallFeedback,                   // 종합 피드백(마크다운). COMPLETED 전이면 null
        LocalDateTime createdAt,
        List<InterviewQuestionResponse> questions // 질문 목록. 생성 직후 PENDING 단계에선 빈 리스트
) {}
