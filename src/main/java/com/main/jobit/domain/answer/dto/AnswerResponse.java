package com.main.jobit.domain.answer.dto;

import com.main.jobit.domain.answer.Answer;
import com.main.jobit.domain.answer.FeedbackStatus;
import com.main.jobit.domain.question.Difficulty;
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
    private String questionCategoryName;
    private Difficulty questionDifficulty;
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
                .questionCategoryName(a.getQuestion().getQuestionCategory().getName())
                .questionDifficulty(a.getQuestion().getDifficulty())
                .authorNickname(a.getUser().getNickname())
                .content(a.getContent())
                .aiFeedback(a.getAiFeedback())
                .feedbackStatus(a.getFeedbackStatus())
                .createdAt(a.getCreatedAt())
                .commentCount(commentCount)
                .build();
    }
}