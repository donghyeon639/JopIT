package com.main.jobit.domain.comment;

import com.main.jobit.domain.comment.dto.CommentCreateRequest;
import com.main.jobit.domain.comment.dto.CommentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// 답변 댓글 관련 REST 엔드포인트. 답변 하위 댓글 목록/작성과 댓글 삭제를 노출한다.
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 특정 답변의 댓글 목록 조회(작성순). 공개 열람.
    @GetMapping("/answers/{answerId}/comments")
    public ResponseEntity<List<CommentResponse>> list(@PathVariable UUID answerId) {
        return ResponseEntity.ok(commentService.getByAnswer(answerId));
    }

    // 댓글 작성. 인증된 사용자명을 함께 넘기며 성공 시 201 Created.
    @PostMapping("/answers/{answerId}/comments")
    public ResponseEntity<CommentResponse> create(
            @PathVariable UUID answerId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CommentCreateRequest request) { // @Valid 로 내용/길이 검증
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(answerId, userDetails.getUsername(), request));
    }

    // 댓글 삭제. 본인 댓글만 삭제 가능하며, 성공 시 본문 없이 204 No Content.
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.delete(commentId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}