package com.main.jobit.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 일반 로그인 요청 바디. @NotBlank로 빈 값/공백 전송을 컨트롤러 진입 전에 차단한다.
// 역직렬화를 위해 기본 생성자가 필요해 @NoArgsConstructor를 둔다.
@Getter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
