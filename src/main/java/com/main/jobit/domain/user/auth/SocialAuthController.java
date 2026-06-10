package com.main.jobit.domain.user.auth;

import com.main.jobit.domain.user.dto.SocialSetupRequest;
import com.main.jobit.domain.user.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 소셜 로그인 회원의 "추가 정보 입력(setup)" 단계를 처리하는 REST 진입점.
// 소셜 신규 가입자는 닉네임/직군이 비어 있을 수 있어(needsProfileUpdate), 이 단계에서 프로필을 완성한다.
// 실제 소셜 로그인 콜백/토큰 발급은 OAuth2 핸들러 + SocialAuthService.loginOrSignup 쪽에서 담당한다.
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    // 프로필 설정. 인증이 필요한 엔드포인트로, 대상 회원은 임의 입력이 아니라
    // JWT로 인증된 본인(@AuthenticationPrincipal)으로 한정한다 — 타인 프로필 수정 방지(보안 핵심).
    // username을 요청 본문이 아닌 인증 주체에서 꺼내는 점이 중요하다.
    @PostMapping("/setup")
    public ResponseEntity<TokenResponse> setupProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid SocialSetupRequest request) {
        return ResponseEntity.ok(socialAuthService.updateProfile(
                userDetails.getUsername(),
                request.getNickname(),
                request.getJobCategoryName()));
    }
}
