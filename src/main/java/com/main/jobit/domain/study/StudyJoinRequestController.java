package com.main.jobit.domain.study;

import com.main.jobit.domain.study.dto.StudyApplyRequest;
import com.main.jobit.domain.study.dto.StudyJoinRequestActionRequest;
import com.main.jobit.domain.study.dto.StudyJoinRequestResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 스터디 참여 신청 REST 컨트롤러 (/api/studies/{studyId} 하위).
 * 신청은 누구나(본인 글 제외), 신청자 목록/처리는 작성자만 — 권한 검증은 Service에서 수행한다.
 */
@RestController
@RequestMapping("/api/studies/{studyId}")
@RequiredArgsConstructor
public class StudyJoinRequestController {

    private final StudyJoinRequestService joinRequestService;

    /** 참여 신청. 본인 모집글·마감·중복·정원 초과 시 409. */
    @PostMapping("/apply")
    public ResponseEntity<StudyJoinRequestResponse> apply(
            @PathVariable UUID studyId,
            @RequestBody(required = false) @Valid StudyApplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 메시지 없이 신청할 수 있도록 body를 선택으로 둠. 누락 시 빈 DTO로 대체해 NPE를 방지.
        StudyApplyRequest body = request != null ? request : new StudyApplyRequest();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(joinRequestService.apply(studyId, body, userDetails.getUsername()));
    }

    /** 신청자 목록 (작성자만). */
    @GetMapping("/applications")
    public ResponseEntity<List<StudyJoinRequestResponse>> list(
            @PathVariable UUID studyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(joinRequestService.listForOwner(studyId, userDetails.getUsername()));
    }

    /** 신청 수락/거절 (작성자만, PENDING 상태에서만). */
    @PatchMapping("/applications/{appId}")
    public ResponseEntity<StudyJoinRequestResponse> decide(
            @PathVariable UUID studyId,
            @PathVariable UUID appId,
            @RequestBody @Valid StudyJoinRequestActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(joinRequestService.decide(
                studyId, appId, request.getAction(), userDetails.getUsername()));
    }
}
