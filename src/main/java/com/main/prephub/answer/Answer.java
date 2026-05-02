package com.main.prephub.answer;

import com.main.prephub.question.Question;
import com.main.prephub.user.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_status", nullable = false, length = 10)
    private FeedbackStatus feedbackStatus = FeedbackStatus.NONE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Answer(Question question, Users user, String content) {
        this.question = question;
        this.user = user;
        this.content = content;
        this.feedbackStatus = FeedbackStatus.NONE;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (feedbackStatus == null) feedbackStatus = FeedbackStatus.NONE;
    }

    public void markFeedbackPending() {
        this.feedbackStatus = FeedbackStatus.PENDING;
    }

    public void applyFeedback(String feedback) {
        this.aiFeedback = feedback;
        this.feedbackStatus = FeedbackStatus.DONE;
    }

    public void markFeedbackFailed() {
        this.feedbackStatus = FeedbackStatus.FAILED;
    }
}