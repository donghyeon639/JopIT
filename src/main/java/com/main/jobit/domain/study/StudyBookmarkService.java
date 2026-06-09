package com.main.jobit.domain.study;

import com.main.jobit.domain.study.dto.StudyListItemResponse;
import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 스터디 북마크(찜) 토글과 내 북마크 목록 조회를 담당하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class StudyBookmarkService {

    private final StudyRepository studyRepository;
    private final StudyBookmarkRepository bookmarkRepository;
    private final StudyJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;

    /**
     * 북마크 토글. 반환값은 토글 후 상태(true=북마크됨).
     * "있으면 삭제, 없으면 추가" — 별도 on/off 파라미터 없이 한 엔드포인트로 처리한다.
     * 삭제 분기에서는 스터디 존재 확인이 불필요하므로, 새로 추가할 때만 studyRepository를 조회한다.
     */
    @Transactional
    public boolean toggle(UUID studyId, String username) {
        Users user = findUser(username);
        Optional<StudyBookmark> existing = bookmarkRepository.findByUserIdAndStudyId(user.getId(), studyId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());   // 이미 북마크됨 → 해제
            return false;
        }
        // 신규 북마크 — 대상 스터디가 실제 존재하는지 확인 후 저장.
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."));
        bookmarkRepository.save(StudyBookmark.builder().user(user).study(study).build());
        return true;
    }

    /**
     * 내 북마크 목록. 북마크된 스터디들을 페이지로 반환 (bookmarked=true 고정).
     * 이 목록은 정의상 전부 북마크된 항목이므로 북마크 여부를 다시 조회하지 않고 true로 둔다.
     * 수락 인원(applied)만 일괄 집계해 카드별 N+1을 방지한다.
     */
    @Transactional(readOnly = true)
    public Page<StudyListItemResponse> listMyBookmarks(String username, Pageable pageable) {
        Users user = findUser(username);
        Page<StudyBookmark> bookmarks =
                bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<Study> studies = bookmarks.getContent().stream().map(StudyBookmark::getStudy).toList();
        Map<UUID, Long> applied = batchAccepted(studies);

        return bookmarks.map(b -> StudyListItemResponse.from(
                b.getStudy(),
                applied.getOrDefault(b.getStudy().getId(), 0L),
                true));   // 북마크 목록 → bookmarked 항상 true
    }

    // ===== 내부 헬퍼 =====

    private Users findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."));
    }

    // 여러 스터디의 ACCEPTED 신청 수를 한 번의 GROUP BY로 집계해 Map으로 변환(StudyService.batchApplied와 동일 의도).
    private Map<UUID, Long> batchAccepted(List<Study> studies) {
        if (studies.isEmpty()) return Map.of();
        List<UUID> ids = studies.stream().map(Study::getId).toList();
        Map<UUID, Long> map = new HashMap<>();
        for (var row : joinRequestRepository.countAcceptedByStudyIds(ids)) {
            map.put(row.getStudyId(), row.getCnt());
        }
        return map;
    }
}
