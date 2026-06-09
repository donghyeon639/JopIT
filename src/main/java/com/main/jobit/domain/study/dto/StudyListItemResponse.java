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

/**
 * 목록 카드용 응답 DTO. Study 엔티티를 그대로 노출하지 않고 화면에 필요한 필드만 추린다.
 * applied(수락 인원)·bookmarked(내 북마크 여부)는 엔티티에 없는 파생 값이라 Service가 일괄 집계해 주입한다.
 */
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
    private long applied;           // 현재 수락(ACCEPTED)된 인원 수. capacity와 함께 "n/정원"으로 표시.
    private LocalDate deadline;
    private StudyStatus status;
    private long viewCount;
    private Set<String> techStacks;
    private Set<String> positions;
    private String author;          // 작성자 닉네임 (프론트 mock 계약과 동일)
    private LocalDateTime createdAt;
    private boolean bookmarked;     // 조회 사용자가 이 스터디를 북마크했는지 (사용자별로 달라지는 값)

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

    // 단건 변환. 엔티티 + 외부에서 계산한 파생값(applied/bookmarked)을 합쳐 DTO 생성.
    // author는 작성자 엔티티가 null일 수 있는 경우를 방어해 닉네임만 꺼낸다.
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

    // 다건 변환. 미리 일괄 집계해 둔 Map/Set을 each-lookup으로 매핑 — 카드마다 개별 쿼리하는 N+1을 피하는 핵심.
    // 집계 결과에 없는 스터디는 신청 0건으로 간주(getOrDefault 0L).
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
