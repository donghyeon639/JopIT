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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/answers/{answerId}/comments")
    public ResponseEntity<List<CommentResponse>> list(@PathVariable UUID answerId) {
        return ResponseEntity.ok(commentService.getByAnswer(answerId));
    }

    @PostMapping("/answers/{answerId}/comments")
    public ResponseEntity<CommentResponse> create(
            @PathVariable UUID answerId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(answerId, userDetails.getUsername(), request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.delete(commentId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}