package com.main.jobit.domain.answer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 답변 작성 요청 바디. content 한 필드만 받으며, 작성자/질문은 경로·인증 정보로 결정된다.
@Getter
@NoArgsConstructor // Jackson 역직렬화를 위한 기본 생성자
public class AnswerCreateRequest {

    // 빈 답변 제출을 막기 위한 검증(공백만 입력해도 거절).
    @NotBlank(message = "답변 내용을 입력해주세요.")
    private String content;
}