package com.main.jobit.domain.study;

import com.main.jobit.domain.user.Users;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 스터디/프로젝트 모집글 엔티티.
 * 작성자(author)가 정원·마감일·기술스택·포지션 조건을 걸어 팀원을 모집하는 글이다.
 * 참여 신청은 StudyJoinRequest, 북마크는 StudyBookmark가 별도 테이블로 관리한다.
 *
 * 인덱스 설계 의도:
 *  - status,deadline : "모집 중 + 마감 임박" 같은 목록 필터/정렬 복합 인덱스
 *  - created_at      : 최신순 목록 정렬(기본 정렬 키)
 *  - view_count      : 인기 모집글(findTop6ByOrderByViewCountDesc) 조회
 *  - author_id       : 작성자 기준 조회 및 소유권 검증 join
 */
@Entity
@Table(
        name = "studies",
        indexes = {
                @Index(name = "idx_studies_status_deadline", columnList = "status,deadline"),
                @Index(name = "idx_studies_created_at", columnList = "created_at"),
                @Index(name = "idx_studies_view_count", columnList = "view_count"),
                @Index(name = "idx_studies_author_id", columnList = "author_id"),
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA 전용 기본 생성자. 외부 직접 생성을 막고 빌더로만 만들도록 강제.
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)   // 애플리케이션/DB가 UUID PK 생성. 순차 노출 방지 목적.
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 모집글 작성자(소유자). LAZY — 목록에서 작성자 닉네임만 필요할 때 불필요한 즉시 로딩 방지.
    // optional=false + nullable=false : 작성자 없는 모집글은 존재할 수 없음.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Users author;

    // enum은 STRING으로 저장 — ORDINAL은 enum 순서 변경 시 기존 데이터가 깨지므로 사용하지 않음.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")   // 소개글은 길이가 가변적이라 TEXT로 저장.
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyMode mode;

    @Column(nullable = false)
    private int capacity;   // 모집 정원. ACCEPTED 신청 수가 이 값에 도달하면 추가 신청/수락이 막힌다.

    @Column(nullable = false)
    private LocalDate deadline;   // 모집 마감일. 생성 시 검증은 DTO의 @FutureOrPresent에서 수행.

    // 모집 상태. columnDefinition에 DB 기본값 'RECRUITING'을 지정해 직접 INSERT 시에도 안전.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20,
            columnDefinition = "varchar(20) not null default 'RECRUITING'")
    private StudyStatus status;

    // 조회수. 상세 조회마다 incrementViewCount(UPDATE)로 증가시켜 동시성 안전하게 누적.
    @Column(name = "view_count", nullable = false)
    private long viewCount;

    // 기술 스택 태그 집합. 별도 컬렉션 테이블(study_tech_stacks)에 (study_id, tech_stack) 행으로 저장.
    // Set + 유니크 제약으로 같은 스터디에 동일 스택 중복 저장을 DB 레벨에서 차단.
    // LAZY — 목록 N건을 조회할 때 매 건마다 컬렉션을 즉시 로딩하지 않기 위함.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "study_tech_stacks",
            joinColumns = @JoinColumn(name = "study_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_study_tech_stacks",
                    columnNames = {"study_id", "tech_stack"})
    )
    @Column(name = "tech_stack", nullable = false, length = 50)
    private Set<String> techStacks = new HashSet<>();

    // 모집 포지션 집합(백엔드/프론트/디자인 등). 구조·의도는 techStacks와 동일.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "study_positions",
            joinColumns = @JoinColumn(name = "study_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_study_positions",
                    columnNames = {"study_id", "position"})
    )
    @Column(name = "position", nullable = false, length = 30)
    private Set<String> positions = new HashSet<>();

    // 생성 시각. updatable=false로 최초 한 번만 기록되고 이후 변경되지 않음.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")   // 수정 시각. @PreUpdate에서 갱신. 최초 생성 시에는 null.
    private LocalDateTime updatedAt;

    // 빌더 생성자. status/viewCount는 외부 입력을 받지 않고 항상 모집 중·0으로 초기화한다(불변식 보장).
    // techStacks/positions는 null 방어 후 기존 빈 컬렉션에 복사 — 필드 컬렉션 참조를 그대로 노출하지 않기 위함.
    @Builder
    public Study(Users author, StudyType type, String title, String summary,
                 StudyMode mode, int capacity, LocalDate deadline,
                 Set<String> techStacks, Set<String> positions) {
        this.author = author;
        this.type = type;
        this.title = title;
        this.summary = summary;
        this.mode = mode;
        this.capacity = capacity;
        this.deadline = deadline;
        this.status = StudyStatus.RECRUITING;   // 생성 시 항상 모집 중으로 시작
        this.viewCount = 0L;
        if (techStacks != null) this.techStacks.addAll(techStacks);
        if (positions != null) this.positions.addAll(positions);
    }

    // 영속화 직전 훅 — 생성 시각/상태 기본값 보정. 빌더를 거치지 않은 경로(JPA 직접 생성 등)에서도 안전하도록 방어.
    @PrePersist
    void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = StudyStatus.RECRUITING;
    }

    // 수정 직전 훅 — 갱신 시각 자동 기록.
    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== 비즈니스 메서드 (상태/권한 검증은 Service에서 수행) =====

    // 전체 필드 교체(PUT 시맨틱). author/status/viewCount는 의도적으로 수정 대상에서 제외해 불변 보장.
    // 컬렉션은 clear() 후 다시 채우는 방식 — 기존 행을 삭제하고 새 값으로 갈아끼워 추가/삭제를 동시에 반영.
    public void update(StudyType type, String title, String summary, StudyMode mode,
                       int capacity, LocalDate deadline,
                       Set<String> techStacks, Set<String> positions) {
        this.type = type;
        this.title = title;
        this.summary = summary;
        this.mode = mode;
        this.capacity = capacity;
        this.deadline = deadline;
        this.techStacks.clear();
        if (techStacks != null) this.techStacks.addAll(techStacks);
        this.positions.clear();
        if (positions != null) this.positions.addAll(positions);
    }

    // 모집 마감. 단방향 전이(RECRUITING → CLOSED)만 제공하며 재오픈 메서드는 없음.
    public void close() {
        this.status = StudyStatus.CLOSED;
    }

    // 소유권 검증. author가 null(이론상 불가)이거나 ID 불일치면 false. 수정/마감/신청처리 권한 판단에 사용.
    public boolean isOwnedBy(UUID userId) {
        return author != null && author.getId().equals(userId);
    }

    public boolean isClosed() {
        return this.status == StudyStatus.CLOSED;
    }
}
