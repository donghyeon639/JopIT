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

@Service
@RequiredArgsConstructor
public class StudyBookmarkService {

    private final StudyRepository studyRepository;
    private final StudyBookmarkRepository bookmarkRepository;
    private final StudyJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;

    /** 북마크 토글. 반환값은 토글 후 상태(true=북마크됨). */
    @Transactional
    public boolean toggle(UUID studyId, String username) {
        Users user = findUser(username);
        Optional<StudyBookmark> existing = bookmarkRepository.findByUserIdAndStudyId(user.getId(), studyId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false;
        }
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."));
        bookmarkRepository.save(StudyBookmark.builder().user(user).study(study).build());
        return true;
    }

    /** 내 북마크 목록. 북마크된 스터디들을 페이지로 반환 (bookmarked=true 고정). */
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
                true));
    }

    // ===== 내부 헬퍼 =====

    private Users findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."));
    }

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
