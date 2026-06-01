package com.main.jobit.domain.study;

import com.main.jobit.domain.study.dto.StudyCreateRequest;
import com.main.jobit.domain.study.dto.StudyDetailResponse;
import com.main.jobit.domain.study.dto.StudyListItemResponse;
import com.main.jobit.domain.study.dto.StudyUpdateRequest;
import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.main.jobit.domain.study.StudySpecifications.bookmarkedBy;
import static com.main.jobit.domain.study.StudySpecifications.modeEquals;
import static com.main.jobit.domain.study.StudySpecifications.positionEquals;
import static com.main.jobit.domain.study.StudySpecifications.statusEquals;
import static com.main.jobit.domain.study.StudySpecifications.techStackEquals;
import static com.main.jobit.domain.study.StudySpecifications.titleOrSummaryContains;
import static com.main.jobit.domain.study.StudySpecifications.typeEquals;

@Service
@RequiredArgsConstructor
public class StudyService {

    /** 정렬 화이트리스트. 외부 입력으로 임의 컬럼 정렬을 막는다. */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "deadline", "viewCount");
    private static final int MAX_PAGE_SIZE = 50;

    private final StudyRepository studyRepository;
    private final StudyJoinRequestRepository joinRequestRepository;
    private final StudyBookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    // ===== 조회 =====

    @Transactional(readOnly = true)
    public Page<StudyListItemResponse> list(StudyType type, StudyMode mode, String techStack,
                                            String position, Boolean recruitingOnly, Boolean bookmarkOnly,
                                            String q, String username, Pageable pageable) {
        Users currentUser = findUser(username);

        // 각 Specification은 인자가 null이면 내부적으로 toPredicate=null을 반환하므로 항상 non-null로 합성.
        Specification<Study> spec = Specification.allOf(
                typeEquals(type),
                modeEquals(mode),
                techStackEquals(techStack),
                positionEquals(position),
                statusEquals(Boolean.TRUE.equals(recruitingOnly) ? StudyStatus.RECRUITING : null),
                bookmarkedBy(Boolean.TRUE.equals(bookmarkOnly) ? currentUser.getId() : null),
                titleOrSummaryContains(q)
        );

        Page<Study> page = studyRepository.findAll(spec, sanitize(pageable));
        List<Study> content = page.getContent();
        Map<UUID, Long> applied = batchApplied(content);
        Set<UUID> bookmarked = batchBookmarked(currentUser.getId(), content);
        return page.map(s -> toListItem(s, applied, bookmarked));
    }

    @Transactional(readOnly = true)
    public List<StudyListItemResponse> popular(String username) {
        Users currentUser = findUser(username);
        List<Study> studies = studyRepository.findTop6ByOrderByViewCountDesc();
        Map<UUID, Long> applied = batchApplied(studies);
        Set<UUID> bookmarked = batchBookmarked(currentUser.getId(), studies);
        return StudyListItemResponse.fromMany(studies, applied, bookmarked);
    }

    @Transactional
    public StudyDetailResponse detail(UUID id, String username) {
        studyRepository.incrementViewCount(id);   // race-safe +1
        Study study = studyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."));
        Users currentUser = findUser(username);
        long applied = joinRequestRepository.countByStudyIdAndStatus(id, JoinRequestStatus.ACCEPTED);
        boolean bookmarked = bookmarkRepository.existsByUserIdAndStudyId(currentUser.getId(), id);
        boolean owner = study.isOwnedBy(currentUser.getId());
        return StudyDetailResponse.from(study, applied, bookmarked, owner);
    }

    // ===== 변경 =====

    @Transactional
    public StudyDetailResponse create(StudyCreateRequest req, String username) {
        Users author = findUser(username);
        Study study = Study.builder()
                .author(author)
                .type(req.getType())
                .title(req.getTitle().trim())
                .summary(req.getSummary().trim())
                .mode(req.getMode())
                .capacity(req.getCapacity())
                .deadline(req.getDeadline())
                .techStacks(req.getTechStacks())
                .positions(req.getPositions())
                .build();
        Study saved = studyRepository.save(study);
        return StudyDetailResponse.from(saved, 0L, false, true);
    }

    @Transactional
    public StudyDetailResponse update(UUID id, StudyUpdateRequest req, String username) {
        Study study = findOwnedStudy(id, username);
        if (study.isClosed()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 마감된 모집글은 수정할 수 없습니다.");
        }
        study.update(req.getType(), req.getTitle().trim(), req.getSummary().trim(),
                req.getMode(), req.getCapacity(), req.getDeadline(),
                req.getTechStacks(), req.getPositions());
        long applied = joinRequestRepository.countByStudyIdAndStatus(id, JoinRequestStatus.ACCEPTED);
        return StudyDetailResponse.from(study, applied, false, true);
    }

    @Transactional
    public void close(UUID id, String username) {
        Study study = findOwnedStudy(id, username);
        if (study.isClosed()) return; // 이미 마감 — 멱등 처리
        study.close();
    }

    // ===== 내부 헬퍼 =====

    private Users findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."));
    }

    private Study findOwnedStudy(UUID id, String username) {
        Study study = studyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."));
        Users currentUser = findUser(username);
        if (!study.isOwnedBy(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수행할 수 있습니다.");
        }
        return study;
    }

    private Pageable sanitize(Pageable raw) {
        int size = Math.min(Math.max(raw.getPageSize(), 1), MAX_PAGE_SIZE);
        Sort sort = filterSort(raw.getSort());
        return PageRequest.of(raw.getPageNumber(), size,
                sort.isSorted() ? sort : Sort.by(Sort.Order.desc("createdAt")));
    }

    private Sort filterSort(Sort raw) {
        if (raw == null || raw.isUnsorted()) return Sort.unsorted();
        List<Sort.Order> allowed = raw.stream()
                .filter(o -> ALLOWED_SORT_FIELDS.contains(o.getProperty()))
                .toList();
        return allowed.isEmpty() ? Sort.unsorted() : Sort.by(allowed);
    }

    /** 페이지 내 N개 스터디의 ACCEPTED 신청 수를 한 번에 집계해 Map으로 반환. */
    private Map<UUID, Long> batchApplied(Collection<Study> studies) {
        if (studies.isEmpty()) return Map.of();
        List<UUID> ids = studies.stream().map(Study::getId).toList();
        Map<UUID, Long> map = new HashMap<>();
        for (var row : joinRequestRepository.countAcceptedByStudyIds(ids)) {
            map.put(row.getStudyId(), row.getCnt());
        }
        return map;
    }

    private Set<UUID> batchBookmarked(UUID userId, Collection<Study> studies) {
        if (studies.isEmpty()) return Set.of();
        List<UUID> ids = studies.stream().map(Study::getId).toList();
        return new HashSet<>(bookmarkRepository.findBookmarkedStudyIds(userId, ids));
    }

    private StudyListItemResponse toListItem(Study s, Map<UUID, Long> appliedMap, Set<UUID> bookmarkedIds) {
        return StudyListItemResponse.from(s,
                appliedMap.getOrDefault(s.getId(), 0L),
                bookmarkedIds.contains(s.getId()));
    }
}
