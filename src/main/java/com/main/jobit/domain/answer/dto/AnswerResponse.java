package com.main.jobit.domain.answer.dto;

import com.main.jobit.domain.answer.Answer;
import com.main.jobit.domain.answer.FeedbackStatus;
import com.main.jobit.domain.question.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// 답변 조회 응답 DTO. 답변 자체 정보에 더해 화면 표시에 필요한 질문 메타데이터(제목/카테고리/난이도)와
// 작성자 닉네임, 피드백 상태/결과, 댓글 수를 평탄화해 담는다. 엔티티 직접 노출을 피하고 LAZY 연관을 미리 해소한다.
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {

    private UUID id;
    private UUID questionId;
    private String questionTitle;          // 질문 제목(목록/상세 표시용)
    private String questionCategoryName;   // 질문 카테고리명
    private Difficulty questionDifficulty; // 질문 난이도(하/중/상)
    private String authorNickname;         // 작성자 닉네임(개인정보인 username 대신 노출)
    private String content;
    private String aiFeedback;             // 피드백 미완료/실패 시 null 일 수 있음
    private FeedbackStatus feedbackStatus; // 클라이언트가 폴링 여부를 판단하는 근거
    private LocalDateTime createdAt;
    private Long commentCount;             // 댓글 수. 생성 직후 등 미집계 시 null

    // 댓글 수가 필요 없는 경우(예: 방금 생성된 답변)용 오버로드. commentCount = null.
    public static AnswerResponse from(Answer a) {
        return from(a, null);
    }

    // 엔티티 → DTO 변환. 연관(question/user)을 여기서 접근하므로 트랜잭션/영속 상태 내에서 호출해야 한다.
    public static AnswerResponse from(Answer a, Long commentCount) {
        return AnswerResponse.builder()
                .id(a.getId())
                .questionId(a.getQuestion().getId())
                .questionTitle(a.getQuestion().getTitle())
                .questionCategoryName(a.getQuestion().getQuestionCategory().getName())
                .questionDifficulty(a.getQuestion().getDifficulty())
                .authorNickname(a.getUser().getNickname())
                .content(a.getContent())
                .aiFeedback(a.getAiFeedback())
                .feedbackStatus(a.getFeedbackStatus())
                .createdAt(a.getCreatedAt())
                .commentCount(commentCount)
                .build();
    }
}