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
// 목록(Summary)과 달리 힌트/모범답안까지 노출하므로 단건 상세 화면에서만 사용한다.
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
    // 카테고리는 id/name을 평탄화해 내보낸다(엔티티 자체를 노출하지 않음 → 지연로딩/순환참조 회피).
    private UUID questionCategoryId;
    private String questionCategoryName;
    private LocalDateTime createdAt;

    // 엔티티 → 상세 DTO 변환. q.getQuestionCategory() 접근이 있으므로
    // 호출 측은 카테고리가 함께 로딩된 상태(@EntityGraph)여야 LAZY 예외/추가 쿼리가 없다.
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