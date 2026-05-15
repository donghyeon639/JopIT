package com.main.jobit.mypage;

import com.main.jobit.mypage.dto.MyStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/stats")
    public ResponseEntity<MyStatsResponse> stats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(myPageService.getStats(userDetails.getUsername()));
    }
}
