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

/**
 * 스터디 모집글 핵심 비즈니스 로직 — 목록/인기/상세 조회와 생성·수정·마감.
 * 신청(StudyJoinRequestService)·북마크(StudyBookmarkService)는 별도 서비스로 분리되어 있고,
 * 여기서는 목록/상세에 필요한 파생값(수락 인원·북마크 여부)을 일괄 집계해 합성한다.
 */
@Service
@RequiredArgsConstructor
public class StudyService {

    /** 정렬 화이트리스트. 외부 입력으로 임의 컬럼 정렬을 막는다(인덱스 없는 컬럼 정렬·정보 노출 방지). */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "deadline", "viewCount");
    private static final int MAX_PAGE_SIZE = 50;   // 과도한 페이지 크기 요청 방어 상한

    private final StudyRepository studyRepository;
    private final StudyJoinRequestRepository joinRequestRepository;
    private final StudyBookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    // ===== 조회 =====

    // 조건 검색 + 페이징 목록. recruitingOnly/bookmarkOnly는 null이면 미적용(전체)으로 본다.
    @Transactional(readOnly = true)
    public Page<StudyListItemResponse> list(StudyType type, StudyMode mode, String techStack,
                                            String position, Boolean recruitingOnly, Boolean bookmarkOnly,
                                            String q, String username, Pageable pageable) {
        Users currentUser = findUser(username);

        // 각 Specification은 인자가 null이면 내부적으로 toPredicate=null을 반환하므로 항상 non-null로 합성.
        // → if 분기 없이 모든 필터를 한 번에 나열할 수 있다. (StudySpecifications의 null 규약 참고)
        Specification<Study> spec = Specification.allOf(
                typeEquals(type),
                modeEquals(mode),
                techStackEquals(techStack),
                positionEquals(position),
                // recruitingOnly가 true일 때만 RECRUITING 조건을 건다. null/false면 마감 글도 포함.
                statusEquals(Boolean.TRUE.equals(recruitingOnly) ? StudyStatus.RECRUITING : null),
                // bookmarkOnly가 true일 때만 "내 북마크" 서브쿼리 조건을 건다.
                bookmarkedBy(Boolean.TRUE.equals(bookmarkOnly) ? currentUser.getId() : null),
                titleOrSummaryContains(q)
        );

        // 정렬·페이지 크기는 sanitize로 화이트리스트/상한을 강제한 뒤 조회.
        Page<Study> page = studyRepository.findAll(spec, sanitize(pageable));
        List<Study> content = page.getContent();
        // 파생값(수락 인원·북마크 여부)을 페이지 단위로 한 번에 집계 → 카드별 N+1 쿼리 방지.
        Map<UUID, Long> applied = batchApplied(content);
        Set<UUID> bookmarked = batchBookmarked(currentUser.getId(), content);
        return page.map(s -> toListItem(s, applied, bookmarked));
    }

    // 인기 모집글(조회수 상위 6건). 메인/대시보드 위젯용. 파생값 집계 방식은 list와 동일.
    @Transactional(readOnly = true)
    public List<StudyListItemResponse> popular(String username) {
        Users currentUser = findUser(username);
        List<Study> studies = studyRepository.findTop6ByOrderByViewCountDesc();
        Map<UUID, Long> applied = batchApplied(studies);
        Set<UUID> bookmarked = batchBookmarked(currentUser.getId(), studies);
        return StudyListItemResponse.fromMany(studies, applied, bookmarked);
    }

    // 상세 조회. 조회수 증가라는 side effect가 있어 readOnly가 아닌 쓰기 트랜잭션이다.
    @Transactional
    public StudyDetailResponse detail(UUID id, String username) {
        studyRepository.incrementViewCount(id);   // race-safe +1 (DB 산술 UPDATE 후 영속성 컨텍스트 clear)
        // clear 이후 findById는 증가된 최신 viewCount를 반영한 엔티티를 다시 읽어온다.
        Study study = studyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."));
        Users currentUser = findUser(username);
        long applied = joinRequestRepository.countByStudyIdAndStatus(id, JoinRequestStatus.ACCEPTED);
        boolean bookmarked = bookmarkRepository.existsByUserIdAndStudyId(currentUser.getId(), id);
        boolean owner = study.isOwnedBy(currentUser.getId());   // 본인 글이면 프론트에서 관리 UI 노출
        return StudyDetailResponse.from(study, applied, bookmarked, owner);
    }

    // ===== 변경 =====

    // 모집글 생성. 인증된 사용자가 작성자가 되며, 신규 글은 항상 신청 0건·미북마크·owner=true로 응답.
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
        return StudyDetailResponse.from(saved, 0L, false, true);   // 갓 만든 글 → 신청 0, 북마크 false, owner true
    }

    // 모집글 수정. 작성자 검증(findOwnedStudy) 후, 이미 마감된 글은 수정 불가(409).
    // 더티 체킹으로 트랜잭션 커밋 시 자동 UPDATE되므로 별도 save 호출이 없다.
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

    // 모집 마감. 작성자만 수행 가능하며, 이미 마감 상태면 아무 것도 하지 않고 종료(멱등).
    @Transactional
    public void close(UUID id, String username) {
        Study study = findOwnedStudy(id, username);
        if (study.isClosed()) return; // 이미 마감 — 멱등 처리(중복 요청에도 같은 결과)
        study.close();
    }

    // ===== 내부 헬퍼 =====

    // username(JWT subject)으로 사용자 조회. 토큰은 유효하나 사용자가 사라진 경우 등을 401로 처리.
    private Users findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."));
    }

    // 수정/마감처럼 작성자 전용 동작의 공통 가드. 없으면 404, 작성자가 아니면 403.
    private Study findOwnedStudy(UUID id, String username) {
        Study study = studyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."));
        Users currentUser = findUser(username);
        if (!study.isOwnedBy(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수행할 수 있습니다.");
        }
        return study;
    }

    // 페이지 요청 정제: 크기를 1~MAX_PAGE_SIZE로 클램프하고, 정렬은 화이트리스트만 통과시킨다.
    // 정렬 지정이 없으면 최신순(createdAt desc)을 기본값으로 강제.
    private Pageable sanitize(Pageable raw) {
        int size = Math.min(Math.max(raw.getPageSize(), 1), MAX_PAGE_SIZE);
        Sort sort = filterSort(raw.getSort());
        return PageRequest.of(raw.getPageNumber(), size,
                sort.isSorted() ? sort : Sort.by(Sort.Order.desc("createdAt")));
    }

    // 허용된 컬럼(createdAt/deadline/viewCount)만 남긴다. 허용 항목이 하나도 없으면 unsorted로 떨어뜨려
    // sanitize에서 기본 정렬이 적용되도록 한다. 임의 컬럼 정렬로 인한 오류/정보 노출 차단.
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

    // 현재 사용자가 이 페이지 내에서 북마크한 스터디 id 집합을 한 번에 조회(카드별 EXISTS N+1 방지).
    private Set<UUID> batchBookmarked(UUID userId, Collection<Study> studies) {
        if (studies.isEmpty()) return Set.of();
        List<UUID> ids = studies.stream().map(Study::getId).toList();
        return new HashSet<>(bookmarkRepository.findBookmarkedStudyIds(userId, ids));
    }

    // 단건 매핑 헬퍼. 집계 결과에서 해당 스터디 값을 꺼내(없으면 0/false) DTO로 변환.
    private StudyListItemResponse toListItem(Study s, Map<UUID, Long> appliedMap, Set<UUID> bookmarkedIds) {
        return StudyListItemResponse.from(s,
                appliedMap.getOrDefault(s.getId(), 0L),
                bookmarkedIds.contains(s.getId()));
    }
}
