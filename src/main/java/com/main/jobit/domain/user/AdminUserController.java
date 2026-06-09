package com.main.jobit.domain.user;

import com.main.jobit.domain.user.Role;
import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
import com.main.jobit.domain.user.dto.UserResponse;
import com.main.jobit.global.security.Admin;
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

// 관리자 전용 회원 관리 API. 회원 목록 조회와 권한 변경을 제공한다.
// 클래스 레벨 @Admin(AOP)으로 ADMIN 권한이 없는 요청은 메서드 실행 전에 차단된다 — 메서드마다 권한 검사를 반복하지 않는다.
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Admin
public class AdminUserController {

    private final UserRepository userRepository;

    // 전체 회원 목록 조회. 엔티티를 그대로 노출하지 않고 UserResponse로 변환해 내려준다.
    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    // 회원 권한(USER/ADMIN) 변경 — 권한 상승이 가능한 민감 동작이므로 @Admin 보호가 필수.
    // 대상이 없으면 예외로 중단하고, 변경 후 갱신된 회원 정보를 반환한다.
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