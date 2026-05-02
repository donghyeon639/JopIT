package com.main.prephub.answer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnswerCreateRequest {

    @NotBlank(message = "답변 내용을 입력해주세요.")
    private String content;
}