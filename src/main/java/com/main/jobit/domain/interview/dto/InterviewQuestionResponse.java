package com.main.jobit.domain.interview.dto;

import java.util.UUID;

public record InterviewQuestionResponse(
        UUID id,
        int orderNo,
        String content,
        String status,
        String transcript,
        String evaluation
) {}
