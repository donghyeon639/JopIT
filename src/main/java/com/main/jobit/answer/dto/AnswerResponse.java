package com.main.jobit.answer.dto;

import com.main.jobit.answer.Answer;
import com.main.jobit.answer.FeedbackStatus;
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
public class AnswerResponse {

    private UUID id;
    private UUID questionId;
    private String questionTitle;
    private String authorNickname;
    private String content;
    private String aiFeedback;
    private FeedbackStatus feedbackStatus;
    private LocalDateTime createdAt;
    private Long commentCount;

    public static AnswerResponse from(Answer a) {
        return from(a, null);
    }

    public static AnswerResponse from(Answer a, Long commentCount) {
        return AnswerResponse.builder()
                .id(a.getId())
                .questionId(a.getQuestion().getId())
                .questionTitle(a.getQuestion().getTitle())
                .authorNickname(a.getUser().getNickname())
                .content(a.getContent())
                .aiFeedback(a.getAiFeedback())
                .feedbackStatus(a.getFeedbackStatus())
                .createdAt(a.getCreatedAt())
                .commentCount(commentCount)
                .build();
    }
}