package com.main.jobit.mypage;

import com.main.jobit.answer.Answer;
import com.main.jobit.answer.AnswerRepository;
import com.main.jobit.answer.FeedbackStatus;
import com.main.jobit.mypage.dto.MyStatsResponse;
import com.main.jobit.user.UserRepository;
import com.main.jobit.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;

    @Transactional(readOnly = true)
    public MyStatsResponse getStats(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<Answer> answers = answerRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        long total = answers.size();
        long feedbackDone = answers.stream()
                .filter(a -> a.getFeedbackStatus() == FeedbackStatus.DONE).count();
        long feedbackPending = answers.stream()
                .filter(a -> a.getFeedbackStatus() == FeedbackStatus.PENDING).count();

        Set<UUID> uniqueQuestions = answers.stream()
                .map(a -> a.getQuestion().getId())
                .collect(Collectors.toCollection(HashSet::new));

        Map<String, Long> byCategory = answers.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getQuestion().getQuestionCategory().getName(),
                        Collectors.counting()));

        Map<String, Long> byDifficulty = answers.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getQuestion().getDifficulty().name(),
                        Collectors.counting()));

        Map<LocalDate, Long> dailyMap = answers.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCreatedAt().toLocalDate(),
                        Collectors.counting()));

        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> last7 = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            last7.put(d, dailyMap.getOrDefault(d, 0L));
        }
        List<MyStatsResponse.DailyCount> last7Days = last7.entrySet().stream()
                .map(e -> new MyStatsResponse.DailyCount(e.getKey(), e.getValue()))
                .toList();

        int streak = computeStreak(dailyMap, today);

        MyStatsResponse.LatestAnswer latest = null;
        if (!answers.isEmpty()) {
            Answer a = answers.get(0);
            latest = new MyStatsResponse.LatestAnswer(
                    a.getId(),
                    a.getQuestion().getId(),
                    a.getQuestion().getTitle(),
                    a.getQuestion().getQuestionCategory().getName(),
                    a.getQuestion().getDifficulty().name(),
                    a.getFeedbackStatus().name(),
                    a.getCreatedAt()
            );
        }

        return new MyStatsResponse(
                total,
                uniqueQuestions.size(),
                feedbackDone,
                feedbackPending,
                streak,
                new HashMap<>(byCategory),
                new HashMap<>(byDifficulty),
                last7Days,
                latest
        );
    }

    private int computeStreak(Map<LocalDate, Long> dailyMap, LocalDate today) {
        int streak = 0;
        LocalDate cursor = today;
        if (!dailyMap.containsKey(cursor)) {
            cursor = cursor.minusDays(1);
        }
        while (dailyMap.containsKey(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}