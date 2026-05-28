package com.main.jobit.global.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AdminAspect의 권한 검증 분기를 DB/스프링 컨텍스트 없이 검증한다.
 * (SecurityContext는 JwtAuthenticationFilter가 채워준다고 가정한 상태를 직접 구성)
 */
class AdminAspectTest {

    private final AdminAspect adminAspect = new AdminAspect();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증 정보가 없으면 401 UNAUTHORIZED")
    void noAuthentication() {
        assertThatThrownBy(adminAspect::checkAdmin)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(401));
    }

    @Test
    @DisplayName("ROLE_USER만 가진 경우 403 FORBIDDEN")
    void notAdmin() {
        setAuthentication("ROLE_USER");

        assertThatThrownBy(adminAspect::checkAdmin)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    @DisplayName("ROLE_ADMIN 권한이 있으면 통과")
    void admin() {
        setAuthentication("ROLE_ADMIN");

        assertThatCode(adminAspect::checkAdmin).doesNotThrowAnyException();
    }

    private void setAuthentication(String authority) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "tester", null, List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}