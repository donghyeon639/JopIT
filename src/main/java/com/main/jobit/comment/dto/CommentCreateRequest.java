package com.main.jobit.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
    private String content;
}