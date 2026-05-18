package com.main.jobit.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndSocialId(AuthProvider provider, String socialId);
    boolean existsByProviderAndSocialId(AuthProvider provider, String socialId);
}
