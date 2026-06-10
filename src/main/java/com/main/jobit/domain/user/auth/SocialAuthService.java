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

// OAuth2 소셜 로그인의 핵심 비즈니스 로직.
// 공급자별로 제각각인 사용자 속성(attributes)을 공통 모델(SocialProfile)로 정규화하고,
// "기존 연동 계정이면 로그인 / 없으면 자동 회원가입" 흐름과 추가 정보 입력(setup)을 처리한다.
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 소셜 인증 성공 후 호출되는 진입점. 조회+(필요 시)가입+저장을 한 트랜잭션으로 묶는다.
    @Transactional
    public TokenResponse loginOrSignup(String provider, Map<String, Object> attributes) {
        // 공급자별 속성 차이를 흡수해 (식별자, 닉네임) 한 쌍으로 정규화.
        SocialProfile profile = extractProfile(provider, attributes);
        AuthProvider authProvider = AuthProvider.fromString(provider);

        // (provider, providerId)로 기존 연동 회원을 찾고, 없으면 그 자리에서 신규 소셜 회원을 만든다.
        Users user = socialAccountRepository.findByProviderAndSocialId(authProvider, profile.providerId())
                .map(SocialAccount::getUser)
                .orElseGet(() -> signupSocialUser(authProvider, profile.providerId(), profile.nickname()));

        return toTokenResponse(user);
    }

    // 소셜 신규 회원 + 연동 계정을 함께 생성한다. (loginOrSignup의 트랜잭션 안에서 호출됨)
    private Users signupSocialUser(AuthProvider authProvider, String providerId, String nickname) {
        // 내부 로그인 아이디는 공급자명+식별자로부터 충돌 없이 생성(아래 buildUniqueUsername).
        String username = buildUniqueUsername(authProvider.name(), providerId);

        // 닉네임 방어: 비어 있으면 "사용자"로, 컬럼 길이(50자)를 넘으면 잘라 저장 오류를 예방.
        String safeNickname = (nickname == null || nickname.isBlank())
                ? "사용자"
                : nickname.strip();
        if (safeNickname.length() > 50) {
            safeNickname = safeNickname.substring(0, 50);
        }

        // 소셜 회원은 비밀번호 로그인을 쓰지 않으므로, 추측 불가능한 랜덤 UUID를 인코딩해 더미 비밀번호로 채운다.
        // jobCategory는 일부러 null — 이후 setup 단계에서 직군을 입력받기 위함(needsProfileUpdate=true 유도).
        Users user = Users.builder()
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname(safeNickname)
                .role(Role.USER)
                .jobCategory(null)
                .build();

        Users savedUser = userRepository.save(user);

        // 회원과 외부 계정의 매핑을 별도 테이블에 기록(향후 동일 회원의 다중 공급자 연동 확장 여지).
        SocialAccount socialAccount = SocialAccount.builder()
                .user(savedUser)
                .provider(authProvider)
                .socialId(providerId)
                .build();
        socialAccountRepository.save(socialAccount);

        return savedUser;
    }

    // 소셜 가입자의 추가 정보 입력(setup) 처리. 컨트롤러에서 인증된 본인 username만 넘어온다.
    @Transactional
    public TokenResponse updateProfile(String username, String nickname, String jobCategoryName) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 선택한 직군이 마스터에 없으면 즉석 생성 후 연결(가입 로직과 동일한 정책).
        JobCategory jobCategory = jobCategoryRepository.findByName(jobCategoryName)
                .orElseGet(() -> jobCategoryRepository.save(JobCategory.builder()
                        .name(jobCategoryName)
                        .build()));

        // 더티 체킹으로 변경 반영 — 트랜잭션 종료 시 자동 UPDATE된다(별도 save 호출 불필요).
        user.updateProfile(nickname, jobCategory);
        return toTokenResponse(user);
    }

    // 회원 → 응답 토큰 변환 공통 헬퍼.
    // needsProfileUpdate: 직군이 비어 있으면 true — 프런트가 setup 화면으로 보내야 할지 판단하는 플래그.
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

    // 공급자별 응답 스키마 차이를 흡수해 공통 SocialProfile(식별자, 표시 닉네임)로 정규화한다.
    // 각 공급자가 주는 고유 식별자 위치(google: sub, github/kakao: id)와 닉네임 후보가 달라 분기 처리.
    private SocialProfile extractProfile(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            // 구글: 식별자는 sub, 닉네임은 name 우선, 없으면 email로 대체.
            case "google" -> new SocialProfile(
                    asString(attributes.get("sub")),
                    firstNonBlank(asString(attributes.get("name")), asString(attributes.get("email")))
            );
            // 깃허브: 식별자는 id, 닉네임은 name 우선, 없으면 login(아이디)으로 대체.
            case "github" -> new SocialProfile(
                    asString(attributes.get("id")),
                    firstNonBlank(asString(attributes.get("name")), asString(attributes.get("login")))
            );
            // 카카오: 닉네임/이메일이 중첩 객체(properties, kakao_account) 안에 있어 한 단계 더 풀어서 꺼낸다.
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
            // 매핑이 정의되지 않은 공급자는 명시적으로 거부(조용히 LOCAL 처리하지 않음).
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인 공급자입니다: " + provider);
        };
    }

    // 외부 식별자로부터 내부 로그인 아이디를 충돌 없이 생성한다.
    private String buildUniqueUsername(String provider, String providerId) {
        // 식별자가 비면 계정을 유일하게 만들 수 없으므로 가입 자체를 중단.
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("소셜 계정 식별자를 찾을 수 없습니다.");
        }

        // "provider_providerId"를 소문자화하고 허용 문자(a-z, 0-9, _) 외에는 _로 치환해 username 규칙에 맞춘다.
        // username 컬럼 길이(30자) 제한에 맞춰 잘라낸다.
        String sanitized = (provider + "_" + providerId)
                .toLowerCase()
                .replaceAll("[^a-z0-9_]", "_");
        if (sanitized.length() > 30) {
            sanitized = sanitized.substring(0, 30);
        }

        // 혹시 이미 같은 아이디가 있으면 "_1", "_2"... 접미사를 붙여 유일해질 때까지 시도한다.
        // 접미사를 붙여도 30자를 넘지 않도록 앞부분(prefix)을 잘라 길이를 확보한다.
        String candidate = sanitized;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            String tail = "_" + suffix++;
            int maxPrefix = Math.max(1, 30 - tail.length());
            candidate = sanitized.substring(0, Math.min(sanitized.length(), maxPrefix)) + tail;
        }
        return candidate;
    }

    // null-안전 문자열 변환 헬퍼. 속성 값이 Integer/Long 등으로 와도 문자열로 통일한다.
    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    // 중첩 속성(Map) 안전 캐스팅. 값이 Map이 아니면 빈 Map을 돌려 NPE 없이 후속 get을 안전하게 만든다.
    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    // 두 후보 중 비어 있지 않은 첫 값을 고르는 헬퍼(닉네임 폴백 체인에 사용).
    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        return second;
    }

    // 공급자 응답에서 추출한 정규화 프로필(식별자, 닉네임)을 담는 내부 전용 불변 레코드.
    private record SocialProfile(String providerId, String nickname) {
    }
}
