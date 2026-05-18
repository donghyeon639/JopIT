package com.main.jobit.domain.answer;

import com.main.jobit.domain.answer.dto.AnswerCreateRequest;
import com.main.jobit.domain.answer.dto.AnswerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<AnswerResponse> create(
            @PathVariable UUID questionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid AnswerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(answerService.create(questionId, userDetails.getUsername(), request));
    }

    @GetMapping("/questions/{questionId}/answers")
    public ResponseEntity<List<AnswerResponse>> listByQuestion(@PathVariable UUID questionId) {
        return ResponseEntity.ok(answerService.getByQuestion(questionId));
    }

    @GetMapping("/answers/{answerId}")
    public ResponseEntity<AnswerResponse> getOne(@PathVariable UUID answerId) {
        return ResponseEntity.ok(answerService.getById(answerId));
    }

    @GetMapping("/answers")
    public ResponseEntity<List<AnswerResponse>> communityFeed() {
        return ResponseEntity.ok(answerService.getCommunityFeed());
    }

    @GetMapping("/answers/me")
    public ResponseEntity<List<AnswerResponse>> myAnswers(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(answerService.getMyAnswers(userDetails.getUsername()));
    }

    @PostMapping("/answers/{answerId}/feedback")
    public ResponseEntity<AnswerResponse> requestFeedback(
            @PathVariable UUID answerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                answerService.requestFeedback(answerId, userDetails.getUsername()));
    }
}