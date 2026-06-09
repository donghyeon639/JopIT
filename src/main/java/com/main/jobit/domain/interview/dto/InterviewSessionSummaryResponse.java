package com.main.jobit.domain.interview.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** 면접 기록 목록용 요약. 질문 본문 없이 메타만 담아 목록 조회를 가볍게 한다. */
public record InterviewSessionSummaryResponse(
        UUID id,                    // 상세 화면으로 진입할 때 쓰는 세션 식별자
        String jobCategory,         // 직군명
        String interviewType,       // InterviewType.name() — 코드/분기용 식별자
        String interviewTypeLabel,  // InterviewType.label() — 화면 표기용 한글 라벨
        String status,              // InterviewStatus.name() — 목록에서 진행/완료 배지 표시용
        LocalDateTime createdAt
) {}
