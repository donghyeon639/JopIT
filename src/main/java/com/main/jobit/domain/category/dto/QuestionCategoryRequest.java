package com.main.jobit.question.dto;

import com.main.jobit.question.QuestionCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCategoryRequest {

    @NotBlank
    private String name;

    public QuestionCategory toEntity() {
        return QuestionCategory.builder().name(name).build();
    }
}
