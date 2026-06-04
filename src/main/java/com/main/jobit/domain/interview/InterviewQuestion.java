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

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(name = "order_no", nullable = false)
    private int orderNo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    @Column(columnDefinition = "TEXT")
    private String evaluation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Builder
    public InterviewQuestion(InterviewSession session, int orderNo, String content) {
        this.session = session;
        this.orderNo = orderNo;
        this.content = content;
        this.status = QuestionStatus.WAITING;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = QuestionStatus.WAITING;
    }

    public void applyEvaluation(String evaluation) {
        this.evaluation = evaluation;
        this.status = QuestionStatus.EVAL_DONE;
    }

    public void markEvalFailed() {
        this.status = QuestionStatus.EVAL_FAILED;
    }
}
