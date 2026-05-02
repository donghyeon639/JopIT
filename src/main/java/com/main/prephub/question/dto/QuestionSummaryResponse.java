package com.main.prephub.question.dto;

import com.main.prephub.question.Difficulty;
import com.main.prephub.question.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 문제 목록 조회용 — hint/modelAnswer 미포함
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSummaryResponse {

    private UUID id;
    private String title;
    private Difficulty difficulty;
    private UUID questionCategoryId;
    private String questionCategoryName;

    public static QuestionSummaryResponse from(Question q) {
        return QuestionSummaryResponse.builder()
                .id(q.getId())
                .title(q.getTitle())
                .difficulty(q.getDifficulty())
                .questionCategoryId(q.getQuestionCategory().getId())
                .questionCategoryName(q.getQuestionCategory().getName())
                .build();
    }
}