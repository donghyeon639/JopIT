package com.main.jobit.domain.mypage;

import com.main.jobit.domain.mypage.dto.MyStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 마이페이지(학습 통계) 진입점(REST). 인증된 본인의 학습 현황만 조회하므로
// 경로 파라미터로 사용자 식별자를 받지 않고, JWT 인증 주체(SecurityContext)에서 사용자명을 꺼내 쓴다.
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    // GET /api/me/stats — 로그인한 본인의 답변·피드백·연속 학습 등 집계 통계 반환.
    // @AuthenticationPrincipal로 현재 인증 주체를 주입받아 getUsername()으로 사용자를 특정한다.
    @GetMapping("/stats")
    public ResponseEntity<MyStatsResponse> stats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(myPageService.getStats(userDetails.getUsername()));
    }
}
