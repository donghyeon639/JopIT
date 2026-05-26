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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectProvider<SocialAuthService> socialAuthServiceProvider;

    @Value("${app.social.redirect-uri:http://localhost:5173/social/callback}")
    private String socialRedirectUri;

    @Value("${app.social.failure-redirect-uri:http://localhost:5173/login?reason=social_failed}")
    private String socialFailureRedirectUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/social/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/tech-trends/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/job-postings/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/social/authorization"))
                    .redirectionEndpoint(endpoint -> endpoint.baseUri("/api/social/callback/*"))
                    .successHandler(this::handleSocialSuccess)
                    .failureHandler((request, response, exception) -> response.sendRedirect(socialFailureRedirectUri))
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://job-it.site",
                "https://www.job-it.site"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void handleSocialSuccess(jakarta.servlet.http.HttpServletRequest request,
                                     jakarta.servlet.http.HttpServletResponse response,
                                     Authentication authentication) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendRedirect(socialFailureRedirectUri);
            return;
        }

        OAuth2User oauth2User = oauthToken.getPrincipal();
        TokenResponse tokenResponse = socialAuthServiceProvider.getObject().loginOrSignup(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauth2User.getAttributes());

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
