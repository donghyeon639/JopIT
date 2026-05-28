package com.main.jobit.domain.study;

import com.main.jobit.domain.study.dto.StudyCreateRequest;
import com.main.jobit.domain.study.dto.StudyDetailResponse;
import com.main.jobit.domain.study.dto.StudyListItemResponse;
import com.main.jobit.domain.study.dto.StudyUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping
    public ResponseEntity<Page<StudyListItemResponse>> list(
            @RequestParam(required = false) StudyType type,
            @RequestParam(required = false) StudyMode mode,
            @RequestParam(required = false) String techStack,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Boolean recruitingOnly,
            @RequestParam(required = false) Boolean bookmarkOnly,
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(studyService.list(
                type, mode, techStack, position, recruitingOnly, bookmarkOnly, q,
                userDetails.getUsername(), pageable));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<StudyListItemResponse>> popular(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyService.popular(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyDetailResponse> detail(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyService.detail(id, userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<StudyDetailResponse> create(
            @RequestBody @Valid StudyCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyService.create(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyDetailResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid StudyUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyService.update(id, request, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> close(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        studyService.close(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
