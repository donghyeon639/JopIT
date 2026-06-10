package com.main.jobit.domain.interview;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 면접 세션 안의 한 질문(=한 턴). 질문 1개당 답변(transcript) 1개·평가(evaluation) 1개를 갖는다.
 * 답변 제출은 {@code InterviewQuestionRepository.submitAnswerIfEligible}의 단일 UPDATE로 원자적으로 전이한다.
 */
@Entity
@Table(name = "interview_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion {

    // PK는 UUID. 답변 제출 엔드포인트의 path 변수로 노출되므로 추측 불가능한 식별자를 쓴다.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 소속 세션. 평가 프롬프트에서 직군/면접 종류 컨텍스트를 끌어오는 데 필요. 본문은 지연 로딩.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    // 세션 내 노출 순서(1부터). 1번은 항상 고정 자기소개 질문이고 2번부터 AI 생성 질문이 채워진다.
    @Column(name = "order_no", nullable = false)
    private int orderNo;

    // 질문 본문.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 사용자 답변(STT로 변환된 텍스트). 답변 제출 전엔 null.
    @Column(columnDefinition = "TEXT")
    private String transcript;

    // LLM이 만든 답변 평가(마크다운). 평가 완료 전엔 null.
    @Column(columnDefinition = "TEXT")
    private String evaluation;

    // 턴 상태 머신(QuestionStatus). 답변 제출 가능 여부·중복 제출 차단의 기준.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    // 질문 생성 시각. updatable=false로 이후 변경 차단.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 답변이 제출된 시각. 제출 전엔 null이며 submitAnswerIfEligible의 UPDATE에서 채워진다.
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    // 생성 전용 빌더. status는 외부에서 받지 않고 항상 WAITING(미답변)으로 시작하도록 강제한다.
    @Builder
    public InterviewQuestion(InterviewSession session, int orderNo, String content) {
        this.session = session;
        this.orderNo = orderNo;
        this.content = content;
        this.status = QuestionStatus.WAITING;
    }

    // 저장 직전 기본값 보정. 빌더를 안 거친 영속화 경로에서도 불변식이 깨지지 않게 방어.
    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = QuestionStatus.WAITING;
    }

    // 비동기 평가 성공 시 호출 — 결과를 저장하며 EVAL_PENDING → EVAL_DONE으로 전이.
    // 주의: transcript/answeredAt은 여기서 건드리지 않는다(원자적 제출 UPDATE에서만 변경됨).
    public void applyEvaluation(String evaluation) {
        this.evaluation = evaluation;
        this.status = QuestionStatus.EVAL_DONE;
    }

    // 평가 LLM 호출 실패 시 호출 — EVAL_FAILED로 전이해 사용자가 답변을 재제출할 수 있게 한다.
    public void markEvalFailed() {
        this.status = QuestionStatus.EVAL_FAILED;
    }
}
