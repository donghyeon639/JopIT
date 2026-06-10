package com.main.jobit.domain.comment;

import com.main.jobit.domain.answer.Answer;
import com.main.jobit.domain.user.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// 답변(Answer)에 달린 댓글을 표현하는 JPA 엔티티.
// 어떤 답변에, 어떤 사용자가, 어떤 내용을 남겼는지를 보관하며,
// 소유자 확인(isOwnedBy)·내용 수정(edit) 같은 도메인 동작을 메서드로 제공한다.
@Entity
@Table(name = "comments")
@Getter
// JPA 전용 기본 생성자. 외부에서의 무분별한 빈 객체 생성 차단(protected).
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 댓글이 달린 대상 답변. 목록 조회 시 N+1 회피를 위해 지연 로딩.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    // 댓글 작성자. 삭제 권한 검사(isOwnedBy)에 사용된다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 댓글 본문(길이 제한은 요청 DTO 단에서 검증).
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 작성 시각. 한 번 기록되면 변경되지 않음(updatable = false).
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Comment(Answer answer, Users user, String content) {
        this.answer = answer;
        this.user = user;
        this.content = content;
    }

    // 영속화 직전 작성 시각 자동 채움.
    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // 댓글 내용 수정. (현재 컨트롤러에는 수정 엔드포인트가 없어 도메인 동작으로만 준비된 상태)
    public void edit(String content) {
        this.content = content;
    }

    // 주어진 사용자가 이 댓글의 작성자인지 확인. 삭제 등 권한 검사에 사용.
    public boolean isOwnedBy(String username) {
        return this.user.getUsername().equals(username);
    }
}