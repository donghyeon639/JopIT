package com.main.jobit.admin;

import com.main.jobit.user.Role;
import com.main.jobit.user.UserRepository;
import com.main.jobit.user.Users;
import com.main.jobit.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable UUID id,
            @RequestParam Role role) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.changeRole(role);
        userRepository.save(user);
        return ResponseEntity.ok(UserResponse.from(user));
    }
}