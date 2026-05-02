package com.main.prephub.question.dto;

import com.main.prephub.question.QuestionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCategoryResponse {

    private UUID id;
    private String name;

    public static QuestionCategoryResponse from(QuestionCategory c) {
        return QuestionCategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .build();
    }
}