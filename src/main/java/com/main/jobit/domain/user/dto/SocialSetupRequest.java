package com.main.jobit.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
