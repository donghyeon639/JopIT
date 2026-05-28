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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Users author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyMode mode;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20,
            columnDefinition = "varchar(20) not null default 'RECRUITING'")
    private StudyStatus status;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
        this.status = StudyStatus.RECRUITING;
        this.viewCount = 0L;
        if (techStacks != null) this.techStacks.addAll(techStacks);
        if (positions != null) this.positions.addAll(positions);
    }

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = StudyStatus.RECRUITING;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== 비즈니스 메서드 (상태/권한 검증은 Service에서 수행) =====

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

    public void close() {
        this.status = StudyStatus.CLOSED;
    }

    public boolean isOwnedBy(UUID userId) {
        return author != null && author.getId().equals(userId);
    }

    public boolean isClosed() {
        return this.status == StudyStatus.CLOSED;
    }
}
