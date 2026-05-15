package com.main.jobit.domain.user;

import lombok.Getter;

@Getter
public enum AuthProvider {
    LOCAL("일반 가입"),
    GOOGLE("구글"),
    KAKAO("카카오"),
    GITHUB("깃허브");

    private final String description;

    AuthProvider(String description) {
        this.description = description;
    }

    public static AuthProvider fromString(String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> GOOGLE;
            case "kakao" -> KAKAO;
            case "github" -> GITHUB;
            default -> LOCAL;
        };
    }
}
