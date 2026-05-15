package com.main.jobit.resume.dto;

public record ResumeFeedbackResponse(
        String feedback,
        int extractedCharCount,
        String detectedFileType
) {}