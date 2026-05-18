package com.main.jobit.domain.resume.dto;

public record ResumeFeedbackResponse(
        String feedback,
        int extractedCharCount,
        String detectedFileType
) {}