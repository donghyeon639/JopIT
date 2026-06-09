package com.main.jobit.domain.user.dto;

import com.main.jobit.domain.user.Role;
import com.main.jobit.domain.user.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// 회원 정보 조회용 응답 DTO. 주로 관리자 콘솔의 회원 목록/상세에 사용된다.
// 비밀번호 등 민감 필드는 의도적으로 제외하고 외부에 노출해도 되는 정보만 담는다(엔티티 직접 노출 금지).
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String username;
    private String nickname;
    private Role role;
    private UUID jobCategoryId;
    private String jobCategoryName;
    private LocalDateTime createdAt;

    // 엔티티 → 응답 DTO 변환 팩토리.
    // jobCategory가 null일 수 있어(소셜 미설정 회원) id/name 모두 null-안전하게 추출한다.
    public static UserResponse from(Users user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .jobCategoryId(user.getJobCategory() != null ? user.getJobCategory().getId() : null)
                .jobCategoryName(user.getJobCategory() != null ? user.getJobCategory().getName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}