package com.main.jobit.domain.category.dto;

import com.main.jobit.domain.category.QuestionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 문제 카테고리 응답 DTO. id/name만 노출(createdAt 등 내부 메타는 응답에서 제외).
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCategoryResponse {

    private UUID id;
    private String name;

    // 엔티티 → 응답 DTO 변환 정적 팩토리.
    public static QuestionCategoryResponse from(QuestionCategory c) {
        return QuestionCategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .build();
    }
}