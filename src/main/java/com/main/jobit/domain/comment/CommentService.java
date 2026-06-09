package com.main.jobit.domain.comment;

import com.main.jobit.domain.answer.Answer;
import com.main.jobit.domain.answer.AnswerRepository;
import com.main.jobit.domain.comment.dto.CommentCreateRequest;
import com.main.jobit.domain.comment.dto.CommentResponse;
import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// 답변에 대한 댓글 작성/조회/삭제를 담당하는 서비스.
// 삭제는 작성자 본인만 가능하도록 도메인 메서드(isOwnedBy)로 권한을 검증한다.
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository; // 댓글이 달릴 대상 답변 존재 확인용
    private final UserRepository userRepository;      // 작성자 조회용

    // 댓글 작성. 대상 답변과 사용자 존재를 검증한 뒤 저장한다.
    @Transactional
    public CommentResponse create(UUID answerId, String username, CommentCreateRequest request) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답변입니다."));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Comment comment = Comment.builder()
                .answer(answer)
                .user(user)
                .content(request.getContent())
                .build();
        commentRepository.save(comment);
        return CommentResponse.from(comment);
    }

    // 특정 답변의 댓글 목록(작성순).
    @Transactional(readOnly = true)
    public List<CommentResponse> getByAnswer(UUID answerId) {
        return commentRepository.findByAnswerIdOrderByCreatedAtAsc(answerId)
                .stream().map(CommentResponse::from).toList();
    }

    // 댓글 삭제. 존재 확인 후 작성자 본인인지 검증하고, 본인이 아니면 거절한다.
    @Transactional
    public void delete(UUID commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        // 타인 댓글 삭제 방지 — 작성자 본인만 허용.
        if (!comment.isOwnedBy(username)) {
            throw new IllegalArgumentException("본인 댓글만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);
    }
}