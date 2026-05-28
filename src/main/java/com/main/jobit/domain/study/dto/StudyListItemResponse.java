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
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyListItemResponse {

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
    private String author;          // 작성자 닉네임 (프론트 mock 계약과 동일)
    private LocalDateTime createdAt;
    private boolean bookmarked;

    public StudyListItemResponse(UUID id, StudyType type, String title, String summary,
                                 StudyMode mode, int capacity, long applied,
                                 LocalDate deadline, StudyStatus status, long viewCount,
                                 Set<String> techStacks, Set<String> positions,
                                 String author, LocalDateTime createdAt, boolean bookmarked) {
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
        this.author = author;
        this.createdAt = createdAt;
        this.bookmarked = bookmarked;
    }

    public static StudyListItemResponse from(Study s, long applied, boolean bookmarked) {
        return StudyListItemResponse.builder()
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
                .author(s.getAuthor() != null ? s.getAuthor().getNickname() : null)
                .createdAt(s.getCreatedAt())
                .bookmarked(bookmarked)
                .build();
    }

    public static List<StudyListItemResponse> fromMany(List<Study> studies,
                                                       java.util.Map<UUID, Long> appliedByStudyId,
                                                       java.util.Set<UUID> bookmarkedIds) {
        return studies.stream()
                .map(s -> from(s,
                        appliedByStudyId.getOrDefault(s.getId(), 0L),
                        bookmarkedIds.contains(s.getId())))
                .toList();
    }
}
