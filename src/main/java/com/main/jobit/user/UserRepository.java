package com.main.jobit.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
}
