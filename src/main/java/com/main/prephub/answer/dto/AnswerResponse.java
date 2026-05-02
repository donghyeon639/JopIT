package com.main.prephub.answer.dto;

import com.main.prephub.answer.Answer;
import com.main.prephub.answer.FeedbackStatus;
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

    public static AnswerResponse from(Answer a) {
        return AnswerResponse.builder()
                .id(a.getId())
                .questionId(a.getQuestion().getId())
                .questionTitle(a.getQuestion().getTitle())
                .authorNickname(a.getUser().getNickname())
                .content(a.getContent())
                .aiFeedback(a.getAiFeedback())
                .feedbackStatus(a.getFeedbackStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }
}