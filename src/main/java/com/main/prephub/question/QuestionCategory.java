package com.main.prephub.question;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "question_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public QuestionCategory(String name) {
        this.name = name;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public void changeName(String name) {
        this.name = name;
    }
}