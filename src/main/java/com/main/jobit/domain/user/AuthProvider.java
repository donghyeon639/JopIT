package com.main.jobit.domain.user;

import lombok.Getter;

// 회원의 가입/인증 경로를 나타내는 열거형.
// 일반 자체 가입(LOCAL)과 OAuth2 소셜 로그인 공급자(GOOGLE/KAKAO/GITHUB)를 구분한다.
// SocialAccount 엔티티에서 (provider, socialId) 조합으로 외부 계정을 식별하는 데 사용된다.
@Getter
public enum AuthProvider {
    LOCAL("일반 가입"),
    GOOGLE("구글"),
    KAKAO("카카오"),
    GITHUB("깃허브");

    // 화면 노출용 한글 설명 (예: 관리자 콘솔/마이페이지에서 가입 경로를 사람이 읽기 좋게 표시)
    private final String description;

    AuthProvider(String description) {
        this.description = description;
    }

    // 스프링 OAuth2가 넘겨주는 registrationId 문자열("google" 등)을 안전하게 enum으로 변환.
    // 대소문자 차이를 없애기 위해 toLowerCase()로 정규화한 뒤 매칭한다.
    // 알 수 없는 값이 들어와도 예외 대신 LOCAL로 폴백하여 호출부가 깨지지 않도록 한다.
    public static AuthProvider fromString(String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> GOOGLE;
            case "kakao" -> KAKAO;
            case "github" -> GITHUB;
            default -> LOCAL;
        };
    }
}
