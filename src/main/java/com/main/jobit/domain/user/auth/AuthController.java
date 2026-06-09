package com.main.jobit.domain.user.auth;

import com.main.jobit.domain.user.dto.LoginRequest;
import com.main.jobit.domain.user.dto.SignupRequest;
import com.main.jobit.domain.user.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 일반(LOCAL) 회원의 가입/로그인 REST 진입점. /api/auth 하위 경로를 담당한다.
// 두 엔드포인트 모두 인증 없이 접근 가능해야 하므로 SecurityConfig에서 permitAll 대상이어야 한다.
// 성공 시 공통적으로 JWT가 담긴 TokenResponse를 반환한다.
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입. @Valid로 요청 본문(아이디/비밀번호/닉네임) 형식을 먼저 검증한 뒤 서비스에 위임한다.
    // 가입 직후 곧바로 로그인된 상태가 되도록 토큰을 함께 발급해 반환한다.
    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@RequestBody @Valid SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    // 로그인. 자격 증명 검증은 서비스 계층에서 수행하며, 실패 시 어떤 항목이 틀렸는지 노출하지 않는다(보안).
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

