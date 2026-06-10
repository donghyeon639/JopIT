package com.main.jobit.global.security;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@link Admin}이 붙은 메서드 또는 클래스의 실행 직전에 ROLE_ADMIN 권한을 검증한다.
 * 권한 정보는 JwtAuthenticationFilter가 SecurityContext에 미리 채워두므로 DB를 다시 조회하지 않는다.
 */
@Aspect
@Component
public class AdminAspect {

    // @annotation: 메서드에 @Admin이 붙은 경우 / @within: 클래스에 @Admin이 붙은 경우.
    // 둘 중 하나라도 매칭되면 대상 메서드 실행 직전(@Before)에 이 검증이 끼어든다.
    @Before("@annotation(com.main.jobit.global.security.Admin) "
            + "|| @within(com.main.jobit.global.security.Admin)")
    public void checkAdmin() {
        // JwtAuthenticationFilter가 미리 채워둔 인증 정보를 SecurityContext에서 읽는다(추가 DB 조회 없음).
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 자체가 없으면 401 — 아직 로그인하지 않은 요청.
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 인증은 됐지만 ROLE_ADMIN 권한이 없으면 403 — 로그인은 했으나 권한 부족.
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
        // 통과하면 별도 반환 없이 원래 메서드가 그대로 실행된다.
    }
}