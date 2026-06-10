package com.main.jobit.domain.question;

import com.main.jobit.domain.category.QuestionCategory;
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

// 면접 질문 엔티티. 카테고리/난이도로 분류되며, 본문(title)·힌트·모범답안(modelAnswer)을 보유한다.
// 사용자는 이 질문에 답변을 작성하고, 모범답안은 상세 조회(관리자/풀이 화면)에서만 노출된다.
@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 전용 기본 생성자
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 소속 카테고리. LAZY 로딩이라 단순 조회 시 N+1이 날 수 있어,
    // 레포지토리에서 @EntityGraph로 함께 fetch한다(QuestionRepository 참고). optional=false → 카테고리 없는 질문 불가.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_category_id", nullable = false)
    private QuestionCategory questionCategory;

    // 질문 제목/본문. 최대 500자.
    @Column(nullable = false, length = 500)
    private String title;

    // 힌트(선택). 길이 제한 없는 TEXT 컬럼.
    @Column(columnDefinition = "TEXT")
    private String hint;

    // 모범답안(선택). TEXT 컬럼. AI 피드백/정답 비교의 기준이 되는 내용.
    @Column(name = "model_answer", columnDefinition = "TEXT")
    private String modelAnswer;

    // 난이도. STRING 매핑이라 enum 상수명이 그대로 저장되어 순서 변경에 안전.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    // 생성 시각. updatable=false로 최초 저장 이후 불변.
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

    // 수정 도메인 메서드: 관리자 수정 시 영속 상태 엔티티의 필드를 한 번에 교체.
    // setter 대신 이 메서드로만 변경을 허용해 변경 지점을 명확히 하고, 더티 체킹으로 UPDATE가 반영된다.
    // (createdAt은 의도적으로 건드리지 않아 생성 시각이 보존됨)
    public void update(QuestionCategory questionCategory, String title, String hint,
                       String modelAnswer, Difficulty difficulty) {
        this.questionCategory = questionCategory;
        this.title = title;
        this.hint = hint;
        this.modelAnswer = modelAnswer;
        this.difficulty = difficulty;
    }
}