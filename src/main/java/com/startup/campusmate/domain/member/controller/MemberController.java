package com.startup.campusmate.domain.member.controller;

import com.startup.campusmate.domain.member.dto.LoginRq;
import com.startup.campusmate.domain.member.dto.LoginRs;
import com.startup.campusmate.domain.member.dto.SignupRq;
import com.startup.campusmate.domain.member.service.MemberService;
import com.startup.campusmate.global.rsData.RsData;
import com.startup.campusmate.standard.base.Empty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/signup")
    public RsData<Empty> signup(
            @RequestBody SignupRq signupRq
    ) {
        memberService.signup(signupRq);
        return RsData.of("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public RsData<LoginRs> login(
            @RequestBody LoginRq loginRq
    ) {
        LoginRs login = memberService.login(loginRq.getEmail(), loginRq.getPassword());
        return RsData.of("로그인 성공", login);
    }

    @PostMapping("/logout")
    public RsData<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-REFRESH-TOKEN") String refreshToken
    ) {
        String accessToken = authHeader.replace("Bearer ", "");
        memberService.logout(accessToken, refreshToken);
        return RsData.of("로그아웃 성공", null);
    }

}
