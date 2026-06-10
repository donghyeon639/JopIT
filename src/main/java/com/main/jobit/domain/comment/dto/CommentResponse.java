package com.main.jobit.domain.comment.dto;

import com.main.jobit.domain.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// 댓글 조회 응답 DTO. 댓글 본문과 함께 어떤 답변의 댓글인지(answerId),
// 작성자 식별/표시 정보(username, nickname)를 평탄화해 담는다.
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private UUID id;
    private UUID answerId;
    private String authorUsername; // 본인 댓글 여부 판별 등 클라이언트 권한 표시에 사용
    private String authorNickname; // 화면 표시용 닉네임
    private String content;
    private LocalDateTime createdAt;

    // 엔티티 → DTO 변환. 연관(answer/user)에 접근하므로 영속 상태(트랜잭션) 내에서 호출해야 한다.
    public static CommentResponse from(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .answerId(c.getAnswer().getId())
                .authorUsername(c.getUser().getUsername())
                .authorNickname(c.getUser().getNickname())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}