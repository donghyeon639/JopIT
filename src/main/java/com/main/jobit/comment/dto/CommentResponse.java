package com.main.jobit.comment.dto;

import com.main.jobit.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private UUID id;
    private UUID answerId;
    private String authorUsername;
    private String authorNickname;
    private String content;
    private LocalDateTime createdAt;

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