package com.main.jobit.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 매 요청 1회 실행되는 JWT 인증 필터(OncePerRequestFilter).
// Authorization 헤더의 Bearer 토큰을 검증해 SecurityContext에 인증 정보를 채운다.
// SecurityConfig에서 UsernamePasswordAuthenticationFilter 앞에 등록되어, 폼 인증보다 먼저 동작한다.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        // 토큰이 있고 유효할 때만 인증을 세팅한다. 없거나 무효면 인증 없이 통과시키고,
        // 최종 인가 판단은 뒤의 authorizeHttpRequests 규칙에 맡긴다(여기서 401을 던지지 않음).
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // DB 조회 없이 JWT 클레임만으로 UserDetails 구성 (principal 타입은 기존과 동일하게 UserDetails 유지)
            UserDetails userDetails = User.builder()
                    .username(username)
                    .password("")
                    .authorities(new SimpleGrantedAuthority("ROLE_" + role))
                    .build();

            // credentials는 null(이미 토큰으로 인증 완료), authorities로 후속 인가가 판단된다.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 이 시점부터 컨트롤러/AdminAspect 등이 SecurityContext에서 현재 사용자/권한을 읽을 수 있다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 인증 설정 여부와 무관하게 다음 필터로 진행.
        filterChain.doFilter(request, response);
    }

    // "Authorization: Bearer <token>" 헤더에서 토큰 부분만 추출. 형식이 안 맞으면 null.
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);   // "Bearer " 접두사(7자) 제거.
        }
        return null;
    }
}


