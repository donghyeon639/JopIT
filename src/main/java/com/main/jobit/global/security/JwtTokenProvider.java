package com.main.jobit.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// JWT 액세스 토큰의 발급·검증·파싱을 담당하는 단일 책임 컴포넌트.
// 토큰에 username(subject)과 role 클레임을 담아 stateless 인증의 근거로 쓴다.
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;        // HMAC 서명 키. 시크릿 문자열에서 1회 파생해 재사용.
    private final long accessTokenValidMs;    // 액세스 토큰 유효기간(ms). 외부 설정값을 ms로 환산해 보관.

    // 생성자 주입. 시크릿은 절대 하드코딩하지 않고 외부 설정(${jwt.secret})에서 받는다.
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-seconds:900}") long accessTokenValiditySeconds   // 기본 15분
    ) {
        // 시크릿 바이트로 HMAC-SHA 키 생성. (jjwt가 키 길이에 따라 알고리즘 자동 선택)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidMs = accessTokenValiditySeconds * 1000;
    }

    // 액세스 토큰 발급. subject=username, 커스텀 클레임 role을 담아 만료시각까지 설정 후 서명.
    public String createAccessToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenValidMs))
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 username(subject) 추출. 서명 검증을 통과해야 값을 얻는다.
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰에서 role 클레임 추출.
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // 토큰 유효성 검사 — 서명 위변조/만료/형식 오류면 예외를 잡아 false로 변환한다(불리언 계약).
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 클라이언트에 만료시각 안내용으로 노출하기 위한 유효기간(초) 조회.
    public long getAccessTokenValiditySeconds() {
        return accessTokenValidMs / 1000;
    }

    // 공통 파싱 헬퍼. verifyWith로 서명을 검증하면서 클레임을 꺼낸다. 검증 실패 시 여기서 예외 발생.
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
