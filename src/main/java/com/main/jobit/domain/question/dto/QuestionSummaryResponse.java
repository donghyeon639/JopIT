package com.main.jobit.domain.question.dto;

import com.main.jobit.domain.question.Difficulty;
import com.main.jobit.domain.question.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 문제 목록 조회용 — hint/modelAnswer 미포함
// 목록에서는 정답(modelAnswer)/힌트를 의도적으로 빼서 응답을 가볍게 하고 정답 노출을 막는다.
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

    // 엔티티 → 요약 DTO 변환. 카테고리 접근이 있으므로 EntityGraph로 함께 로딩된 상태 전제.
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