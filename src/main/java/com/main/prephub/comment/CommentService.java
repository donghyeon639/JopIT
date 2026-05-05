package com.main.prephub.comment;

import com.main.prephub.answer.Answer;
import com.main.prephub.answer.AnswerRepository;
import com.main.prephub.comment.dto.CommentCreateRequest;
import com.main.prephub.comment.dto.CommentResponse;
import com.main.prephub.user.UserRepository;
import com.main.prephub.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

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

    @Transactional(readOnly = true)
    public List<CommentResponse> getByAnswer(UUID answerId) {
        return commentRepository.findByAnswerIdOrderByCreatedAtAsc(answerId)
                .stream().map(CommentResponse::from).toList();
    }

    @Transactional
    public void delete(UUID commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        if (!comment.isOwnedBy(username)) {
            throw new IllegalArgumentException("본인 댓글만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);
    }
}