package com.main.jobit.domain.user.dto;

import com.main.jobit.domain.user.Role;
import com.main.jobit.domain.user.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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