package com.main.jobit.mypage.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    public record DailyCount(LocalDate date, long count) {}

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
