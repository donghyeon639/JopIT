package com.main.jobit.domain.category;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

// 면접 질문의 분류(CS/DB/네트워크/OS 등)를 표현하는 엔티티.
// 직군(JobCategory)과는 다른 축의 분류이며, Question이 이 카테고리를 ManyToOne으로 참조한다.
@Entity
@Table(name = "question_categories")
@Getter
// JPA 전용 기본 생성자(외부 사용 차단). 정상 생성은 @Builder 경로로만.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 카테고리명. unique 제약으로 중복 카테고리명을 DB 레벨에서 차단.
    @Column(nullable = false, length = 50, unique = true)
    private String name;

    // 생성 시각. updatable=false라 최초 저장 이후 불변.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public QuestionCategory(String name) {
        this.name = name;
    }

    // 영속화 직전 생성 시각 자동 세팅(이미 값이 있으면 보존).
    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // 이름 변경 도메인 메서드. setter를 노출하지 않고 의도가 드러나는 메서드로만 수정 허용.
    // 관리자 카테고리 수정 시 영속 상태 엔티티에서 호출 → 더티 체킹으로 UPDATE 반영.
    public void changeName(String name) {
        this.name = name;
    }
}