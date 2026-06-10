package com.main.jobit.domain.category.dto;

import com.main.jobit.domain.category.QuestionCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 문제 카테고리 생성/수정 요청 바디 DTO.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCategoryRequest {

    // 카테고리명 필수 — 공백/빈 문자열이면 400으로 거른다(@Valid와 함께 동작).
    @NotBlank
    private String name;

    // 생성 요청 시 엔티티 변환에 사용. 수정 요청에서는 changeName으로 직접 반영하므로 미사용.
    public QuestionCategory toEntity() {
        return QuestionCategory.builder().name(name).build();
    }
}
