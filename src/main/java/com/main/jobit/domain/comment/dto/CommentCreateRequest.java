package com.main.jobit.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 댓글 작성 요청 바디. content 한 필드만 받으며 작성자/대상 답변은 인증·경로로 결정된다.
@Getter
@NoArgsConstructor // Jackson 역직렬화용 기본 생성자
public class CommentCreateRequest {

    // 빈 댓글 방지 + 과도하게 긴 댓글 방지(최대 500자).
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
    private String content;
}