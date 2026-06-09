package com.main.jobit.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 소셜 연동 계정(SocialAccount)에 대한 JPA 리포지토리.
// 조회 기준은 항상 (provider, socialId) 복합 키 — 엔티티의 유니크 제약과 동일한 조합이다.
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    // 소셜 로그인 시 기존 연동 회원을 찾는 핵심 쿼리. 결과가 없으면 신규 가입 분기로 이어진다.
    Optional<SocialAccount> findByProviderAndSocialId(AuthProvider provider, String socialId);
    // 연동 여부만 빠르게 확인할 때 사용(엔티티 적재 없이 존재 여부만 판단).
    boolean existsByProviderAndSocialId(AuthProvider provider, String socialId);
}
