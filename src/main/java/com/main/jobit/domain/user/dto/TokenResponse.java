package com.main.jobit.domain.user.dto;

import com.main.jobit.domain.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 인증 성공(회원가입/로그인/소셜 로그인) 시 공통으로 내려가는 응답 DTO.
// 토큰뿐 아니라 프런트가 즉시 화면을 구성할 수 있도록 닉네임/권한/직군 등 표시용 정보를 함께 담는다.
@Getter
@Builder
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;        // 발급된 JWT 액세스 토큰
    private String tokenType;          // 토큰 타입(항상 "Bearer")
    private long expiresIn;            // 액세스 토큰 유효 기간(초)
    private String nickname;
    private String username;
    private Role role;                 // 권한 등급. 프런트의 관리자 메뉴 노출 등에 사용
    private String jobCategoryName;    // 선택 직군 이름(없으면 null)
    private boolean needsProfileUpdate; // true면 소셜 가입자가 추가 정보(직군) 입력이 필요한 상태
}