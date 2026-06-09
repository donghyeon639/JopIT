package com.main.jobit.domain.mypage;

import com.main.jobit.domain.answer.Answer;
import com.main.jobit.domain.answer.AnswerRepository;
import com.main.jobit.domain.answer.FeedbackStatus;
import com.main.jobit.domain.mypage.dto.MyStatsResponse;
import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
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

// 마이페이지 학습 통계 집계 서비스.
// 사용자의 전체 답변을 한 번에 조회한 뒤, DB 추가 쿼리 없이 메모리(Stream)에서 여러 관점으로 집계한다
// (전체/순수 문항 수, 피드백 상태별, 카테고리별, 난이도별, 최근 7일 추이, 연속 학습일, 최신 답변).
// 읽기 전용 트랜잭션이라 답변→문항→카테고리 지연 로딩 접근이 같은 영속성 컨텍스트 안에서 안전하게 일어난다.
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;

    @Transactional(readOnly = true)
    public MyStatsResponse getStats(String username) {
        // 인증 주체의 username으로 사용자 엔티티를 찾는다(없으면 잘못된 토큰/삭제된 사용자).
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 최신순 정렬로 전체 답변을 한 번에 로드 — 이후 모든 집계는 이 리스트를 재사용(쿼리 1회).
        // 최신순이라 list.get(0)이 곧 "가장 최근 답변"이 된다(아래 latest 계산에서 활용).
        List<Answer> answers = answerRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        long total = answers.size();
        // 피드백 상태별 개수: DONE(완료) / PENDING(대기). 비동기 AI 피드백 진행 현황을 보여주기 위함.
        long feedbackDone = answers.stream()
                .filter(a -> a.getFeedbackStatus() == FeedbackStatus.DONE).count();
        long feedbackPending = answers.stream()
                .filter(a -> a.getFeedbackStatus() == FeedbackStatus.PENDING).count();

        // 같은 문항에 여러 번 답해도 1개로 카운트 — 실제로 "풀어본 문항 수"를 Set으로 중복 제거해 구한다.
        Set<UUID> uniqueQuestions = answers.stream()
                .map(a -> a.getQuestion().getId())
                .collect(Collectors.toCollection(HashSet::new));

        // 카테고리명별 답변 수 집계(CS/DB/네트워크 등 학습 분포 시각화용).
        Map<String, Long> byCategory = answers.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getQuestion().getQuestionCategory().getName(),
                        Collectors.counting()));

        // 난이도(하/중/상)별 답변 수 집계. enum 상수명을 키로 사용.
        Map<String, Long> byDifficulty = answers.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getQuestion().getDifficulty().name(),
                        Collectors.counting()));

        // 날짜(시각의 날짜 부분)별 답변 수 — 잔디(히트맵)/스트릭 계산의 기초 자료.
        Map<LocalDate, Long> dailyMap = answers.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCreatedAt().toLocalDate(),
                        Collectors.counting()));

        // 최근 7일 추이: 답변이 없던 날도 0으로 채워 빠짐없는 7개 칸을 만든다.
        // TreeMap이라 날짜 오름차순 정렬이 보장되고, 6일 전→오늘 순으로 채운다.
        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> last7 = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            last7.put(d, dailyMap.getOrDefault(d, 0L));  // 해당 날짜 기록이 없으면 0
        }
        List<MyStatsResponse.DailyCount> last7Days = last7.entrySet().stream()
                .map(e -> new MyStatsResponse.DailyCount(e.getKey(), e.getValue()))
                .toList();

        // 연속 학습일(스트릭) 계산은 별도 헬퍼로 분리.
        int streak = computeStreak(dailyMap, today);

        // 최신 답변 요약 카드: 답변이 하나도 없으면 null로 두어 응답에서 "없음"을 표현.
        // answers가 최신순이므로 인덱스 0이 가장 최근 답변이다.
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

        // 집계 결과를 응답 DTO로 묶어 반환.
        // byCategory/byDifficulty는 Collectors가 만든 구현체 대신 HashMap으로 복사해 직렬화 시 안정적으로 다룬다.
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

    // 연속 학습일 계산: 오늘(또는 오늘 기록이 없으면 어제)부터 하루씩 거슬러 올라가며
    // 답변 기록이 끊기기 전까지의 연속 일수를 센다.
    private int computeStreak(Map<LocalDate, Long> dailyMap, LocalDate today) {
        int streak = 0;
        LocalDate cursor = today;
        // 오늘 아직 답변을 안 했더라도 어제까지 이어온 스트릭은 유지되도록 기준점을 하루 당긴다.
        if (!dailyMap.containsKey(cursor)) {
            cursor = cursor.minusDays(1);
        }
        // 기록이 있는 날이 연속되는 동안 카운트하고, 빈 날을 만나면 즉시 종료.
        while (dailyMap.containsKey(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}