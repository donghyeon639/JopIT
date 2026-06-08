package com.main.jobit.domain.interview.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** 면접 기록 목록용 요약. 질문 본문 없이 메타만 담아 목록 조회를 가볍게 한다. */
public record InterviewSessionSummaryResponse(
        UUID id,
        String jobCategory,
        String interviewType,
        String interviewTypeLabel,
        String status,
        LocalDateTime createdAt
) {}
