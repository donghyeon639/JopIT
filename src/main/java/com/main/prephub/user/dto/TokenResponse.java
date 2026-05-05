package com.main.prephub.user.dto;

import com.main.prephub.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String nickname;
    private String username;
    private Role role;
    private String jobCategoryName;
}