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

// 답변 관련 REST 엔드포인트. 질문 하위 답변 작성/목록과 답변 단건/피드/내 답변/피드백 요청을 노출한다.
// 인증이 필요한 작업은 @AuthenticationPrincipal 로 현재 로그인 사용자명을 받아 서비스에 전달한다.
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    // 특정 질문에 답변 작성. 성공 시 201 Created 로 생성된 답변을 반환한다.
    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<AnswerResponse> create(
            @PathVariable UUID questionId,
            @AuthenticationPrincipal UserDetails userDetails,   // 인증 주체에서 작성자명 추출
            @RequestBody @Valid AnswerCreateRequest request) {  // @Valid 로 빈 본문 검증
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(answerService.create(questionId, userDetails.getUsername(), request));
    }

    // 특정 질문의 답변 목록 조회(최신순). 인증 불필요(공개 열람).
    @GetMapping("/questions/{questionId}/answers")
    public ResponseEntity<List<AnswerResponse>> listByQuestion(@PathVariable UUID questionId) {
        return ResponseEntity.ok(answerService.getByQuestion(questionId));
    }

    // 답변 단건 상세 조회.
    @GetMapping("/answers/{answerId}")
    public ResponseEntity<AnswerResponse> getOne(@PathVariable UUID answerId) {
        return ResponseEntity.ok(answerService.getById(answerId));
    }

    // 커뮤니티 피드: 전체 답변 최신순.
    @GetMapping("/answers")
    public ResponseEntity<List<AnswerResponse>> communityFeed() {
        return ResponseEntity.ok(answerService.getCommunityFeed());
    }

    // 마이페이지: 로그인 사용자가 작성한 답변 목록.
    @GetMapping("/answers/me")
    public ResponseEntity<List<AnswerResponse>> myAnswers(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(answerService.getMyAnswers(userDetails.getUsername()));
    }

    // AI 피드백 요청. 본인 답변만 가능하며, 즉시 처리되지 않고 비동기로 진행되므로
    // 응답에는 보통 PENDING 상태가 담긴다(클라이언트는 이후 폴링/재조회로 결과 확인).
    @PostMapping("/answers/{answerId}/feedback")
    public ResponseEntity<AnswerResponse> requestFeedback(
            @PathVariable UUID answerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                answerService.requestFeedback(answerId, userDetails.getUsername()));
    }
}