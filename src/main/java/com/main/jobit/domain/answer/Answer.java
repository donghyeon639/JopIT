package com.main.jobit.domain.answer;

import com.main.jobit.domain.question.Question;
import com.main.jobit.domain.user.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// 사용자가 특정 면접 질문(Question)에 대해 작성한 답변을 표현하는 JPA 엔티티.
// 답변 본문뿐 아니라 비동기 AI 피드백 결과(aiFeedback)와 그 진행 상태(feedbackStatus)를
// 함께 보관한다. 피드백은 별도 비동기 흐름(AiFeedbackService)에서 채워지므로,
// 상태 전이 메서드를 통해서만 변경하도록 setter 대신 의도가 드러나는 도메인 메서드를 제공한다.
@Entity
@Table(name = "answers")
@Getter
// 기본 생성자는 JPA 프록시/리플렉션 용도로만 필요하므로 외부에서 빈 객체를 만들지 못하도록 protected 로 막는다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer {

    // PK 는 DB가 생성하는 시퀀스 대신 UUID 를 사용한다(분산 환경/노출 식별자 추측 방지에 유리).
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 답변이 달린 대상 질문. 목록 조회 등에서 N+1 을 피하기 위해 지연 로딩(LAZY) 사용.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // 답변 작성자. 본인 확인(피드백 요청 권한 등)에 사용된다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 답변 본문. 길이 제한이 없으므로 TEXT 컬럼으로 매핑.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // AI 피드백 결과 텍스트. 피드백이 완료되기 전(NONE/PENDING)이거나 실패 시에는 null 일 수 있다.
    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    // 피드백 진행 상태(NONE→PENDING→DONE/FAILED). 비동기 처리의 핵심 상태값이므로
    // 문자열로 저장(EnumType.STRING)해 enum 순서가 바뀌어도 DB 값이 깨지지 않게 한다.
    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_status", nullable = false, length = 10)
    private FeedbackStatus feedbackStatus = FeedbackStatus.NONE;

    // 생성 시각. updatable = false 로 두어 한 번 기록되면 수정되지 않도록 한다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 답변 생성용 빌더. 피드백 관련 필드는 외부에서 받지 않고 항상 NONE 으로 시작한다(초기 상태 강제).
    @Builder
    public Answer(Question question, Users user, String content) {
        this.question = question;
        this.user = user;
        this.content = content;
        this.feedbackStatus = FeedbackStatus.NONE;
    }

    // 영속화 직전 기본값 보정. createdAt 미설정 시 현재 시각으로,
    // feedbackStatus 가 null 로 들어온 예외적 경우에도 NONE 으로 안전하게 채운다.
    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (feedbackStatus == null) feedbackStatus = FeedbackStatus.NONE;
    }

    // === 피드백 상태 전이 메서드 (비동기 AI 피드백 플로우에서만 호출) ===

    // 피드백 요청 접수 시: 처리 대기 상태로 전환. 중복 요청 방지는 호출부(쿼리)에서 처리한다.
    public void markFeedbackPending() {
        this.feedbackStatus = FeedbackStatus.PENDING;
    }

    // 피드백 생성 성공 시: 결과 텍스트를 담고 완료(DONE) 상태로 전환.
    public void applyFeedback(String feedback) {
        this.aiFeedback = feedback;
        this.feedbackStatus = FeedbackStatus.DONE;
    }

    // 피드백 생성 실패 시: FAILED 로 표시해 두면 이후 재요청(NONE/FAILED만 허용) 대상이 된다.
    public void markFeedbackFailed() {
        this.feedbackStatus = FeedbackStatus.FAILED;
    }
}