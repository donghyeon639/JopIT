package com.main.jobit.domain.study.dto;

import com.main.jobit.domain.study.Study;
import com.main.jobit.domain.study.StudyMode;
import com.main.jobit.domain.study.StudyStatus;
import com.main.jobit.domain.study.StudyType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * 상세 응답. 목록 응답과 동일 필드 + 향후 소유권 검증 마이그레이션을 위해 authorId 포함.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyDetailResponse {

    private UUID id;
    private StudyType type;
    private String title;
    private String summary;
    private StudyMode mode;
    private int capacity;
    private long applied;
    private LocalDate deadline;
    private StudyStatus status;
    private long viewCount;
    private Set<String> techStacks;
    private Set<String> positions;
    private UUID authorId;          // 작성자 식별자. 프론트 소유권 판단/마이그레이션 대비용.
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean bookmarked;     // 조회 사용자의 북마크 여부
    private boolean owner;          // 조회 사용자가 작성자 본인인지 — 수정/마감/신청관리 버튼 노출 제어에 사용

    public StudyDetailResponse(UUID id, StudyType type, String title, String summary,
                               StudyMode mode, int capacity, long applied,
                               LocalDate deadline, StudyStatus status, long viewCount,
                               Set<String> techStacks, Set<String> positions,
                               UUID authorId, String author,
                               LocalDateTime createdAt, LocalDateTime updatedAt,
                               boolean bookmarked, boolean owner) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.summary = summary;
        this.mode = mode;
        this.capacity = capacity;
        this.applied = applied;
        this.deadline = deadline;
        this.status = status;
        this.viewCount = viewCount;
        this.techStacks = techStacks;
        this.positions = positions;
        this.authorId = authorId;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.bookmarked = bookmarked;
        this.owner = owner;
    }

    // 엔티티 + 사용자 컨텍스트(applied/bookmarked/owner)를 합쳐 상세 DTO 생성.
    public static StudyDetailResponse from(Study s, long applied, boolean bookmarked, boolean owner) {
        return StudyDetailResponse.builder()
                .id(s.getId())
                .type(s.getType())
                .title(s.getTitle())
                .summary(s.getSummary())
                .mode(s.getMode())
                .capacity(s.getCapacity())
                .applied(applied)
                .deadline(s.getDeadline())
                .status(s.getStatus())
                .viewCount(s.getViewCount())
                .techStacks(s.getTechStacks())
                .positions(s.getPositions())
                .authorId(s.getAuthor() != null ? s.getAuthor().getId() : null)
                .author(s.getAuthor() != null ? s.getAuthor().getNickname() : null)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .bookmarked(bookmarked)
                .owner(owner)
                .build();
    }
}
