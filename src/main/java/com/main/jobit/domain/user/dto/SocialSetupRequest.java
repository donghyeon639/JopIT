package com.main.jobit.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 소셜 가입자의 추가 정보 입력(setup) 요청 바디.
// SignupRequest와 달리 직군(jobCategoryName)이 필수 — 소셜 가입 시점엔 직군이 비어 있어 이 단계에서 반드시 채운다.
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialSetupRequest {
    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotBlank(message = "직군 선택은 필수입니다.")
    private String jobCategoryName;
}
