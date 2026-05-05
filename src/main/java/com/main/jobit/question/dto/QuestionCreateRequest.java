package com.main.jobit.question.dto;

import com.main.jobit.question.Difficulty;
import com.main.jobit.question.Question;
import com.main.jobit.question.QuestionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateRequest {

    @NotNull
    private UUID questionCategoryId;

    @NotBlank
    private String title;

    private String hint;

    @NotBlank
    private String modelAnswer;

    @NotNull
    private Difficulty difficulty;

    public Question toEntity(QuestionCategory questionCategory) {
        return Question.builder()
                .questionCategory(questionCategory)
                .title(title)
                .hint(hint)
                .modelAnswer(modelAnswer)
                .difficulty(difficulty)
                .build();
    }
}