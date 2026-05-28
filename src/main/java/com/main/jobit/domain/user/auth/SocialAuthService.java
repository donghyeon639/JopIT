package com.main.jobit.domain.user.auth;

import com.main.jobit.domain.job.JobCategory;
import com.main.jobit.domain.job.JobCategoryRepository;
import com.main.jobit.global.security.JwtTokenProvider;
import com.main.jobit.domain.user.AuthProvider;
import com.main.jobit.domain.user.Role;
import com.main.jobit.domain.user.SocialAccount;
import com.main.jobit.domain.user.SocialAccountRepository;
import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
import com.main.jobit.domain.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse loginOrSignup(String provider, Map<String, Object> attributes) {
        SocialProfile profile = extractProfile(provider, attributes);
        AuthProvider authProvider = AuthProvider.fromString(provider);

        Users user = socialAccountRepository.findByProviderAndSocialId(authProvider, profile.providerId())
                .map(SocialAccount::getUser)
                .orElseGet(() -> signupSocialUser(authProvider, profile.providerId(), profile.nickname()));

        return toTokenResponse(user);
    }

    private Users signupSocialUser(AuthProvider authProvider, String providerId, String nickname) {
        String username = buildUniqueUsername(authProvider.name(), providerId);

        String safeNickname = (nickname == null || nickname.isBlank())
                ? "사용자"
                : nickname.strip();
        if (safeNickname.length() > 50) {
            safeNickname = safeNickname.substring(0, 50);
        }

        Users user = Users.builder()
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname(safeNickname)
                .role(Role.USER)
                .jobCategory(null)
                .build();

        Users savedUser = userRepository.save(user);

        SocialAccount socialAccount = SocialAccount.builder()
                .user(savedUser)
                .provider(authProvider)
                .socialId(providerId)
                .build();
        socialAccountRepository.save(socialAccount);

        return savedUser;
    }

    @Transactional
    public TokenResponse updateProfile(String username, String nickname, String jobCategoryName) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        JobCategory jobCategory = jobCategoryRepository.findByName(jobCategoryName)
                .orElseGet(() -> jobCategoryRepository.save(JobCategory.builder()
                        .name(jobCategoryName)
                        .build()));

        user.updateProfile(nickname, jobCategory);
        return toTokenResponse(user);
    }

    private TokenResponse toTokenResponse(Users user) {
        String token = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValiditySeconds())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .role(user.getRole())
                .jobCategoryName(user.getJobCategory() != null ? user.getJobCategory().getName() : null)
                .needsProfileUpdate(user.getJobCategory() == null)
                .build();
    }

    private SocialProfile extractProfile(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> new SocialProfile(
                    asString(attributes.get("sub")),
                    firstNonBlank(asString(attributes.get("name")), asString(attributes.get("email")))
            );
            case "github" -> new SocialProfile(
                    asString(attributes.get("id")),
                    firstNonBlank(asString(attributes.get("name")), asString(attributes.get("login")))
            );
            case "kakao" -> {
                Map<String, Object> properties = asMap(attributes.get("properties"));
                Map<String, Object> account = asMap(attributes.get("kakao_account"));
                yield new SocialProfile(
                        asString(attributes.get("id")),
                        firstNonBlank(
                                asString(properties.get("nickname")),
                                asString(account.get("email"))
                        )
                );
            }
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인 공급자입니다: " + provider);
        };
    }

    private String buildUniqueUsername(String provider, String providerId) {
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("소셜 계정 식별자를 찾을 수 없습니다.");
        }

        String sanitized = (provider + "_" + providerId)
                .toLowerCase()
                .replaceAll("[^a-z0-9_]", "_");
        if (sanitized.length() > 30) {
            sanitized = sanitized.substring(0, 30);
        }

        String candidate = sanitized;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            String tail = "_" + suffix++;
            int maxPrefix = Math.max(1, 30 - tail.length());
            candidate = sanitized.substring(0, Math.min(sanitized.length(), maxPrefix)) + tail;
        }
        return candidate;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        return second;
    }

    private record SocialProfile(String providerId, String nickname) {
    }
}
