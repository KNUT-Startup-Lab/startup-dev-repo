package com.startup.campusmate.domain.member.controller;

import com.startup.campusmate.domain.member.dto.auth.session.LoginRq;
import com.startup.campusmate.domain.member.dto.auth.session.LoginRs;
import com.startup.campusmate.domain.member.service.MemberService;
import com.startup.campusmate.global.rsData.RsData;
import com.startup.campusmate.standard.base.Empty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthMemberController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<RsData<LoginRs>> login(@RequestBody LoginRq loginRq) {
        LoginRs login = memberService.login(loginRq.getEmail(), loginRq.getPassword());
        return ResponseEntity.ok(RsData.of("로그인 성공", login));
    }

    @PostMapping("/logout")
    public ResponseEntity<RsData<Empty>> logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("X-REFRESH-TOKEN") String refreshToken
    ) {
        memberService.logout(accessToken, refreshToken);
        return ResponseEntity.ok(RsData.of("로그아웃 성공"));
    }

//    @PostMapping("/verify-phone")
//    public ResponseEntity<RsData<Boolean>> verifyPhone(@RequestBody VerifyPhone verifyPhone) {
//        // 휴대폰 인증하는 코드
//        return RsData.of("인증성공", isVerified);
//    }

}


