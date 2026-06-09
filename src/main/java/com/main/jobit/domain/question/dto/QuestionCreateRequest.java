package com.main.jobit.domain.question.dto;

import com.main.jobit.domain.question.Difficulty;
import com.main.jobit.domain.question.Question;
import com.main.jobit.domain.category.QuestionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 문제 생성/수정 공용 요청 DTO(관리자). @Valid와 함께 동작하는 검증 제약 포함.
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateRequest {

    // 소속 카테고리 id 필수. 실제 존재 여부는 서비스에서 조회로 추가 검증한다.
    @NotNull
    private UUID questionCategoryId;

    // 제목 필수(빈 문자열 불가)
    @NotBlank
    private String title;

    // 힌트는 선택 항목 — 검증 없음.
    private String hint;

    // 모범답안 필수(AI 피드백/정답 비교의 기준이라 비울 수 없음)
    @NotBlank
    private String modelAnswer;

    // 난이도 필수
    @NotNull
    private Difficulty difficulty;

    // 요청 → 엔티티 변환. 카테고리는 서비스에서 조회·검증한 영속 엔티티를 주입받는다(연관관계 세팅).
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