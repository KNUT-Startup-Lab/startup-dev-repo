package com.startup.campusmate.domain.social.controller;

import com.startup.campusmate.domain.social.dto.SocialLoginRq;
import com.startup.campusmate.domain.social.dto.SocialLoginRs;
import com.startup.campusmate.domain.social.service.SocialService;
import com.startup.campusmate.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/social")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialService socialService;

    @GetMapping("/{provider}/url")
    public ResponseEntity<RsData<String>> getSocialLoginUrl(@PathVariable String provider) {
        String url = socialService.getLoginUrl(provider);
        return ResponseEntity.ok(RsData.of("소셜 로그인 URL", url));
    }

    @PostMapping("/{provider}/login")
    public ResponseEntity<RsData<SocialLoginRs>> loginBySocialToken(
            @PathVariable String provider,
            @RequestBody SocialLoginRq rq
    ) {
        SocialLoginRs result = socialService.loginWithToken(provider, rq);
        return ResponseEntity.ok(RsData.of("소셜 로그인 성공", result));
    }
}
