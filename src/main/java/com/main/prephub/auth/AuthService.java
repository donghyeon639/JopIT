package com.main.prephub.auth;

import com.main.prephub.job.JobCategory;
import com.main.prephub.job.JobCategoryRepository;
import com.main.prephub.security.JwtTokenProvider;
import com.main.prephub.user.Role;
import com.main.prephub.user.UserRepository;
import com.main.prephub.user.Users;
import com.main.prephub.user.dto.LoginRequest;
import com.main.prephub.user.dto.SignupRequest;
import com.main.prephub.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        String categoryName = (request.getJobCategoryName() != null && !request.getJobCategoryName().isBlank())
                ? request.getJobCategoryName() : "백엔드";
        JobCategory jobCategory = jobCategoryRepository.findByName(categoryName)
                .orElseGet(() -> jobCategoryRepository.save(
                        JobCategory.builder().name(categoryName).build()));

        Users user = Users.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(Role.USER)
                .jobCategory(jobCategory)
                .build();

        userRepository.save(user);

        String token = jwtTokenProvider.createAccessToken(user.getUsername());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValiditySeconds())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Users user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtTokenProvider.createAccessToken(user.getUsername());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValiditySeconds())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
