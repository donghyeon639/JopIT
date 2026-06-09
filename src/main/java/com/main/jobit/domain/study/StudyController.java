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

/**
 * 스터디 모집글 REST 컨트롤러 (/api/studies).
 * 모든 엔드포인트는 인증 사용자를 전제로 하며, @AuthenticationPrincipal로 현재 사용자를 받아 Service에 위임한다.
 * 검증/권한/예외는 Service 계층에서 처리하고, 컨트롤러는 요청 매핑·HTTP 상태 코드만 담당한다.
 */
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    // 목록 조회. 모든 필터 파라미터는 선택(required=false)이며, 페이징 기본값은 12건·최신순.
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

    // 인기 모집글(조회수 상위 6건). 페이징 없는 고정 길이 리스트.
    // 주의: literal "popular"가 "{id}"(UUID)보다 먼저 선언되어 매핑 충돌이 없도록 위치시킴.
    @GetMapping("/popular")
    public ResponseEntity<List<StudyListItemResponse>> popular(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyService.popular(userDetails.getUsername()));
    }

    // 상세 조회. Service에서 조회수 +1 side effect가 발생한다.
    @GetMapping("/{id}")
    public ResponseEntity<StudyDetailResponse> detail(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyService.detail(id, userDetails.getUsername()));
    }

    // 모집글 생성. 검증 통과 시 201 Created + 생성된 상세 응답.
    @PostMapping
    public ResponseEntity<StudyDetailResponse> create(
            @RequestBody @Valid StudyCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyService.create(request, userDetails.getUsername()));
    }

    // 모집글 수정(전체 교체, PUT). 작성자 권한·마감 여부는 Service에서 검증.
    @PutMapping("/{id}")
    public ResponseEntity<StudyDetailResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid StudyUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyService.update(id, request, userDetails.getUsername()));
    }

    // 모집 마감. 상태만 바꾸는 부분 변경이라 PATCH, 응답 본문이 없어 204 No Content.
    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> close(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        studyService.close(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
