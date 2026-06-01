package com.main.jobit.domain.study;

import com.main.jobit.domain.study.dto.StudyListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyBookmarkController {

    private final StudyBookmarkService bookmarkService;

    /**
     * 북마크 토글.
     * 응답: {"bookmarked": true} 또는 {"bookmarked": false} (토글 후 상태).
     */
    @PostMapping("/{studyId}/bookmark")
    public ResponseEntity<Map<String, Boolean>> toggle(
            @PathVariable UUID studyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean bookmarked = bookmarkService.toggle(studyId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
    }

    /**
     * 내 북마크 목록. /api/studies/bookmarks 는 literal segment이므로
     * /api/studies/{id} (UUID 패턴)보다 우선 매칭된다.
     */
    @GetMapping("/bookmarks")
    public ResponseEntity<Page<StudyListItemResponse>> listMine(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookmarkService.listMyBookmarks(userDetails.getUsername(), pageable));
    }
}
