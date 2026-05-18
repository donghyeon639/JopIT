package com.main.jobit.domain.question.dto;

import com.main.jobit.domain.question.Difficulty;
import com.main.jobit.domain.question.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// 문제 상세 조회용 — hint/modelAnswer 포함
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDetailResponse {

    private UUID id;
    private String title;
    private String hint;
    private String modelAnswer;
    private Difficulty difficulty;
    private UUID questionCategoryId;
    private String questionCategoryName;
    private LocalDateTime createdAt;

    public static QuestionDetailResponse from(Question q) {
        return QuestionDetailResponse.builder()
                .id(q.getId())
                .title(q.getTitle())
                .hint(q.getHint())
                .modelAnswer(q.getModelAnswer())
                .difficulty(q.getDifficulty())
                .questionCategoryId(q.getQuestionCategory().getId())
                .questionCategoryName(q.getQuestionCategory().getName())
                .createdAt(q.getCreatedAt())
                .build();
    }
}