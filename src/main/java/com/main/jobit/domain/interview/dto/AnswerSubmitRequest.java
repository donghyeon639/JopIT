package com.main.jobit.domain.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 음성 답변을 브라우저에서 텍스트로 변환(STT)한 결과를 제출하는 요청.
 */
public record AnswerSubmitRequest(
        // STT 결과 텍스트. 빈 답변 차단(@NotBlank) + LLM 프롬프트 비대화/악용 방지를 위해 길이 상한(@Size).
        @NotBlank(message = "답변 내용을 입력해주세요.")
        @Size(max = 5000, message = "답변은 5000자까지 입력할 수 있습니다.")
        String transcript
) {}
