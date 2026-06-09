package com.main.jobit.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// 댓글 영속성 계층. 답변별 댓글 목록 조회와 개수 집계를 제공한다.
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // 특정 답변의 댓글을 작성순(오래된 것부터)으로 조회. 대화 흐름을 위해 오름차순 사용.
    List<Comment> findByAnswerIdOrderByCreatedAtAsc(UUID answerId);

    // 답변별 댓글 수. AnswerService 의 응답 DTO 에서 commentCount 채울 때 사용된다.
    long countByAnswerId(UUID answerId);
}