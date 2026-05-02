package com.main.prephub.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(max = 20, message = "아이디는 20자 이하여야 합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 15, message = "닉네임은 15자 이하여야 합니다.")
    private String nickname;

    // 대분류 이름 (예: 개발자, 디자이너, 기획자, 마케터)
    private String jobCategoryName;
}
