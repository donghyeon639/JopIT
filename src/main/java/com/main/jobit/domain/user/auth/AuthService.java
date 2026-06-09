package com.main.jobit.domain.user.auth;

import com.main.jobit.domain.job.JobCategory;
import com.main.jobit.domain.job.JobCategoryRepository;
import com.main.jobit.global.security.JwtTokenProvider;
import com.main.jobit.domain.user.Role;
import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
import com.main.jobit.domain.user.dto.LoginRequest;
import com.main.jobit.domain.user.dto.SignupRequest;
import com.main.jobit.domain.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 일반(LOCAL) 회원의 가입/로그인 비즈니스 로직을 담는 서비스.
// 비밀번호 인코딩, 중복 검증, JWT 발급 등 인증의 핵심 규칙이 모인 곳.
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입. 쓰기 트랜잭션 — 중복 검증 → 직군 확보 → 회원 저장 → 토큰 발급이 한 트랜잭션으로 묶인다.
    @Transactional
    public TokenResponse signup(SignupRequest request) {
        // 아이디/닉네임 중복은 DB 유니크 제약 이전에 친절한 메시지로 선제 차단(경합 시 최종 방어선은 DB 제약).
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 직군 미선택 시 기본 직군 "백엔드"로 폴백(서비스 1차 타깃이 백엔드 직군이라는 정책 반영).
        String categoryName = (request.getJobCategoryName() != null && !request.getJobCategoryName().isBlank())
                ? request.getJobCategoryName() : "백엔드";
        // 이미 존재하면 재사용, 없으면 즉석 생성 — 직군 마스터를 미리 채워두지 않아도 가입이 가능하도록 한다.
        JobCategory jobCategory = jobCategoryRepository.findByName(categoryName)
                .orElseGet(() -> jobCategoryRepository.save(
                        JobCategory.builder().name(categoryName).build()));

        // 비밀번호는 반드시 인코딩하여 저장(평문 저장 금지). role은 일반 사용자로 고정.
        Users user = Users.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(Role.USER)
                .jobCategory(jobCategory)
                .build();

        userRepository.save(user);

        // 가입 즉시 사용할 액세스 토큰 발급. 권한(role)을 클레임에 실어 이후 인가에 활용한다.
        String token = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValiditySeconds())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .role(user.getRole())
                .jobCategoryName(user.getJobCategory() != null ? user.getJobCategory().getName() : null)
                .build();
    }

    // 로그인. 조회만 수행하므로 readOnly 트랜잭션으로 최적화.
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // 아이디 없음/비밀번호 불일치 모두 동일한 메시지로 처리 — 어떤 항목이 틀렸는지 흘리지 않아 계정 열거 공격을 방지한다.
        Users user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 평문 비교가 아니라 인코딩된 해시와 대조(passwordEncoder.matches). 저장된 해시는 절대 복호화하지 않는다.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValiditySeconds())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .role(user.getRole())
                .jobCategoryName(user.getJobCategory() != null ? user.getJobCategory().getName() : null)
                .build();
    }
}
