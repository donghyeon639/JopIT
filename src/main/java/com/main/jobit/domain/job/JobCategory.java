package com.main.jobit.domain.job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

// 직군(백엔드/프런트엔드/데이터 등)을 표현하는 엔티티.
// 문제 카테고리(QuestionCategory)와는 별개 개념 — 이쪽은 "직군" 분류이고
// 관리자 콘솔의 /api/admin/categories 에서 CRUD로 관리한다.
@Entity
@Table(name = "job_categories")
@Getter
// 기본 생성자는 JPA 프록시 생성용으로만 필요 → 외부에서 빈 객체 생성 못 하도록 PROTECTED.
// 정상 생성 경로는 아래 @Builder 생성자 하나로 강제한다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid") // PostgreSQL의 네이티브 uuid 타입으로 매핑
    private UUID id;

    // 직군명. unique 제약으로 DB 레벨에서 중복 직군명 방지(길이 50자 제한).
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // 생성 시각. updatable=false로 한 번 저장되면 수정 불가(불변 메타데이터).
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 생성 시 외부에서 받는 값은 name 하나뿐 — id/createdAt은 영속화 시점에 자동 채워진다.
    @Builder
    public JobCategory(String name) {
        this.name = name;
    }

    // 영속화 직전 콜백: createdAt이 비어 있을 때만 현재 시각을 채운다.
    // (null 체크를 두어 테스트 등에서 미리 세팅한 값이 있으면 덮어쓰지 않음)
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}