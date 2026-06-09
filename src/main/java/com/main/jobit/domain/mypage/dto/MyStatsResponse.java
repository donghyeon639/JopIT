package com.main.jobit.domain.mypage.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// 마이페이지 학습 통계 응답 DTO. MyPageService가 집계한 여러 관점의 수치를 한 번에 담는다.
// totalAnswers      : 작성한 답변 총개수(중복 문항 포함)
// uniqueQuestions   : 풀어본 서로 다른 문항 수(중복 제거)
// feedbackDone      : AI 피드백 완료(DONE)된 답변 수
// feedbackPending   : AI 피드백 대기(PENDING) 중인 답변 수
// currentStreakDays : 오늘 기준 연속 학습일
// byCategory        : 카테고리명 → 답변 수
// byDifficulty      : 난이도명 → 답변 수
// last7Days         : 최근 7일 일별 답변 수(빈 날도 0으로 채워진 7개)
// latestAnswer      : 가장 최근 답변 요약(없으면 null)
public record MyStatsResponse(
        long totalAnswers,
        long uniqueQuestions,
        long feedbackDone,
        long feedbackPending,
        int currentStreakDays,
        Map<String, Long> byCategory,
        Map<String, Long> byDifficulty,
        List<DailyCount> last7Days,
        LatestAnswer latestAnswer
) {
    // 일별 답변 수 한 칸(날짜 + 그날의 답변 개수) — 최근 7일 추이 그래프의 데이터 포인트.
    public record DailyCount(LocalDate date, long count) {}

    // 최신 답변 요약 카드용. 답변/문항 식별자와 함께 문항 제목·카테고리·난이도·피드백 상태를
    // 평탄화(flatten)해 담아, 프런트가 추가 조회 없이 바로 렌더링할 수 있게 한다.
    public record LatestAnswer(
            java.util.UUID answerId,
            java.util.UUID questionId,
            String questionTitle,
            String questionCategoryName,
            String questionDifficulty,
            String feedbackStatus,
            java.time.LocalDateTime createdAt
    ) {}
}
