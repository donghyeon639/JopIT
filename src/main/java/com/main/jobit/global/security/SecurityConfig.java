package com.main.jobit.global.security;

import com.main.jobit.domain.user.auth.SocialAuthService;
import com.main.jobit.domain.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

// Spring Security 핵심 설정. 인가 정책, JWT 필터 등록, OAuth2 소셜 로그인, CORS, 비밀번호 인코더를 한곳에서 구성.
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    // SocialAuthService를 ObjectProvider로 받는 이유: 빈 초기화 순환 의존을 피하고 실제 사용 시점에 지연 조회하기 위함.
    private final ObjectProvider<SocialAuthService> socialAuthServiceProvider;

    // 소셜 로그인 성공/실패 후 프런트로 리다이렉트할 주소. 환경별로 외부 설정에서 주입(기본은 로컬 개발용).
    @Value("${app.social.redirect-uri:http://localhost:5173/social/callback}")
    private String socialRedirectUri;

    @Value("${app.social.failure-redirect-uri:http://localhost:5173/login?reason=social_failed}")
    private String socialFailureRedirectUri;

    // 보안 필터 체인 정의. 요청이 통과하는 인증/인가 규칙의 핵심.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화: 세션 쿠키가 아닌 JWT(Bearer 헤더) 기반 stateless API라 CSRF 토큰이 불필요.
            .csrf(AbstractHttpConfigurer::disable)
            // 프런트(다른 오리진)에서 호출하므로 CORS 정책 적용.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 서버 세션을 만들지 않는 STATELESS 정책 — 인증 상태는 매 요청 JWT로만 판단.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // URL별 인가 규칙(위에서 아래로 먼저 매칭되는 규칙 적용).
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()                      // 회원가입/로그인 등 인증 진입점은 공개
                    .requestMatchers("/api/social/**").permitAll()                    // 소셜 로그인 시작/콜백 공개
                    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()    // 카테고리 조회는 비로그인 허용
                    .requestMatchers(HttpMethod.GET, "/api/tech-trends/**").permitAll()   // 기술 트렌드 조회 공개
                    .requestMatchers(HttpMethod.GET, "/api/job-postings/**").permitAll()  // 채용 공고 조회 공개(GET 한정)
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")                // 관리자 API는 ROLE_ADMIN 필수(@Admin AOP와 다층 방어)
                    .anyRequest().authenticated()                                     // 그 외 모든 요청은 인증 필요
            )
            // OAuth2 소셜 로그인 설정. 기본 엔드포인트를 /api/social/* 로 바꿔 프런트 라우팅과 맞춘다.
            .oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/social/authorization"))   // 인가 요청 시작 URL
                    .redirectionEndpoint(endpoint -> endpoint.baseUri("/api/social/callback/*"))         // provider 콜백 수신 URL
                    .successHandler(this::handleSocialSuccess)                                           // 성공 시 JWT 발급 후 프런트로 리다이렉트
                    .failureHandler((request, response, exception) -> response.sendRedirect(socialFailureRedirectUri))
            )
            // JWT 필터를 폼 인증 필터 앞에 배치 — 토큰이 있으면 폼 인증 단계 전에 인증을 끝낸다.
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // JWT 필터를 빈으로 등록(필터 내부 의존성 주입을 위해 직접 생성).
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    // 비밀번호 단방향 해시 인코더. 회원가입 시 저장·로그인 시 대조 모두 BCrypt 사용.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 폼/아이디·비번 로그인 처리를 위한 AuthenticationManager 노출. 인증 API에서 주입받아 사용.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // CORS 허용 정책. 로컬 개발 + 운영 도메인만 화이트리스트로 허용한다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",     // Vite 개발 서버
                "https://job-it.site",       // 운영
                "https://www.job-it.site"    // 운영(www)
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);    // 자격 증명 허용(Authorization 헤더 등). 그래서 origin은 와일드카드가 아닌 명시 목록이어야 함.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);   // 전체 경로에 동일 정책 적용.
        return source;
    }

    // 소셜 로그인 성공 후처리: provider에서 받은 사용자 정보로 가입/로그인 처리하고 자체 JWT를 발급한 뒤
    // 토큰·프로필 정보를 쿼리 파라미터에 실어 프런트 콜백 URL로 리다이렉트한다(SPA가 토큰을 저장).
    private void handleSocialSuccess(jakarta.servlet.http.HttpServletRequest request,
                                     jakarta.servlet.http.HttpServletResponse response,
                                     Authentication authentication) throws IOException {
        // OAuth2 인증 토큰이 아니면 비정상 흐름 — 실패 페이지로 보낸다.
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendRedirect(socialFailureRedirectUri);
            return;
        }

        // provider 식별자(google/kakao 등)와 사용자 속성을 도메인 서비스에 넘겨 가입 또는 로그인 처리.
        OAuth2User oauth2User = oauthToken.getPrincipal();
        TokenResponse tokenResponse = socialAuthServiceProvider.getObject().loginOrSignup(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauth2User.getAttributes());

        // 발급된 토큰·프로필을 쿼리 파라미터로 인코딩해 프런트 콜백으로 전달.
        // needsProfileUpdate는 최초 소셜 가입자가 추가 정보 입력 화면으로 가야 하는지를 프런트에 알린다.
        String targetUrl = UriComponentsBuilder.fromUriString(socialRedirectUri)
                .queryParam("accessToken", tokenResponse.getAccessToken())
                .queryParam("nickname", tokenResponse.getNickname())
                .queryParam("username", tokenResponse.getUsername())
                .queryParam("role", tokenResponse.getRole())
                .queryParam("jobCategoryName", tokenResponse.getJobCategoryName())
                .queryParam("needsProfileUpdate", tokenResponse.isNeedsProfileUpdate())
                .encode()
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }
}
