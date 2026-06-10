package com.main.jobit.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 일반 회원가입 요청 바디. 검증 규칙은 엔티티 컬럼 제약 및 보안 정책과 맞춰져 있다.
// (아이디 길이는 username 컬럼 30자 이내, 비밀번호 최소 8자는 최소 강도 정책.)
@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(max = 20, message = "아이디는 20자 이하여야 합니다.")
    private String username;

    // 최소 길이만 검증하고 인코딩은 서비스 계층에서 수행한다(평문은 여기까지만 존재).
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 15, message = "닉네임은 15자 이하여야 합니다.")
    private String nickname;

    // 대분류 이름 (예: 개발자, 디자이너, 기획자, 마케터)
    // 선택 입력 — 비어 있으면 서비스에서 기본 직군("백엔드")으로 폴백한다.
    private String jobCategoryName;
}
