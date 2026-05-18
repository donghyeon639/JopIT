package com.main.jobit.domain.user.auth;

import com.main.jobit.domain.user.dto.SocialSetupRequest;
import com.main.jobit.domain.user.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    @PostMapping("/setup")
    public ResponseEntity<TokenResponse> setupProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid SocialSetupRequest request) {
        return ResponseEntity.ok(socialAuthService.updateProfile(
                userDetails.getUsername(), 
                request.getNickname(), 
                request.getJobCategoryName()));
    }
}
