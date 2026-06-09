package com.main.jobit.global.security;

import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Spring Security가 username/password 인증(폼/로그인 API) 시 사용자 정보를 조회하는 진입점.
// DB의 Users 엔티티를 Security가 이해하는 UserDetails로 변환해 준다.
// (JWT 요청은 JwtAuthenticationFilter가 토큰만으로 인증을 채우므로 이 서비스를 타지 않는다 — 로그인 시점에만 사용)
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // username으로 사용자를 찾아 UserDetails로 매핑. AuthenticationManager가 이 결과의 password와 입력값을 대조한다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 없는 사용자는 UsernameNotFoundException — Security 표준 흐름상 인증 실패로 처리된다.
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // roles(...)는 내부적으로 "ROLE_" 접두사를 붙인다. enum 이름(USER/ADMIN)을 그대로 권한명으로 사용.
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())   // 이미 BCrypt로 인코딩된 해시. 평문이 아니다.
                .roles(user.getRole().name())
                .build();
    }
}
