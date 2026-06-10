package com.main.jobit.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// 회원(Users)에 대한 JPA 리포지토리. 식별자 타입은 UUID(엔티티 PK와 동일).
public interface UserRepository extends JpaRepository<Users, UUID> {
    // 로그인/인증 주체 조회에 사용. username은 유니크 컬럼이므로 단건 Optional 반환.
    Optional<Users> findByUsername(String username);
    // 회원가입 시 아이디 중복 검증용.
    boolean existsByUsername(String username);
    // 회원가입 시 닉네임 중복 검증용.
    boolean existsByNickname(String nickname);
}
