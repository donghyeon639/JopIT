package com.main.jobit.domain.interview;

import com.main.jobit.domain.interview.dto.AnswerSubmitRequest;
import com.main.jobit.domain.interview.dto.InterviewSessionResponse;
import com.main.jobit.domain.interview.dto.InterviewSessionSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /** 면접 세션 생성 — 이력서 파일 + 직군 + 면접 종류. 질문 생성은 비동기라 202와 함께 PENDING 상태를 반환. */
    @PostMapping(value = "/sessions", consumes = "multipart/form-data")
    public ResponseEntity<InterviewSessionResponse> createSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobCategory") String jobCategory,
            @RequestParam("interviewType") String interviewType) {
        InterviewSessionResponse response = interviewService.createSession(
                userDetails.getUsername(), file, jobCategory, interviewType);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /** 세션 조회 — 질문/답변/평가/상태. 프런트가 질문 생성·평가 완료를 폴링하는 데 사용. */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<InterviewSessionResponse> getSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(interviewService.getSession(userDetails.getUsername(), sessionId));
    }

    /** 답변 제출(STT 변환 텍스트). 평가는 비동기라 202와 함께 EVAL_PENDING 상태를 반환. */
    @PostMapping("/sessions/{sessionId}/questions/{questionId}/answer")
    public ResponseEntity<InterviewSessionResponse> submitAnswer(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID sessionId,
            @PathVariable UUID questionId,
            @RequestBody @Valid AnswerSubmitRequest request) {
        InterviewSessionResponse response = interviewService.submitAnswer(
                userDetails.getUsername(), sessionId, questionId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /** 면접 종료 → 종합 피드백 비동기 생성. 202와 함께 현재 상태 반환(overallFeedback은 폴링으로 채워짐). */
    @PostMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<InterviewSessionResponse> complete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID sessionId) {
        InterviewSessionResponse response = interviewService.completeSession(
                userDetails.getUsername(), sessionId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /** 내 면접 기록 목록. */
    @GetMapping("/sessions")
    public ResponseEntity<List<InterviewSessionSummaryResponse>> mySessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(interviewService.getMySessions(userDetails.getUsername()));
    }
}
