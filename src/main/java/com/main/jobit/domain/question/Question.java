package com.main.jobit.question;

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

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_category_id", nullable = false)
    private QuestionCategory questionCategory;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String hint;

    @Column(name = "model_answer", columnDefinition = "TEXT")
    private String modelAnswer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Question(QuestionCategory questionCategory, String title, String hint,
                    String modelAnswer, Difficulty difficulty) {
        this.questionCategory = questionCategory;
        this.title = title;
        this.hint = hint;
        this.modelAnswer = modelAnswer;
        this.difficulty = difficulty;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void update(QuestionCategory questionCategory, String title, String hint,
                       String modelAnswer, Difficulty difficulty) {
        this.questionCategory = questionCategory;
        this.title = title;
        this.hint = hint;
        this.modelAnswer = modelAnswer;
        this.difficulty = difficulty;
    }
}