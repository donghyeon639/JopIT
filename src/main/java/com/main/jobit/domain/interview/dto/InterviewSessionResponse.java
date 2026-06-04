package com.main.jobit.domain.interview.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InterviewSessionResponse(
        UUID id,
        String jobCategory,
        String interviewType,
        String interviewTypeLabel,
        String status,
        String overallFeedback,
        LocalDateTime createdAt,
        List<InterviewQuestionResponse> questions
) {}
