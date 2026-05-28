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

    @Before("@annotation(com.main.jobit.global.security.Admin) "
            + "|| @within(com.main.jobit.global.security.Admin)")
    public void checkAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }
}