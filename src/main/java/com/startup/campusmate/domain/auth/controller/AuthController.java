package com.startup.campusmate.domain.auth.controller;

import com.startup.campusmate.domain.auth.dto.recovery.FindIdRq;
import com.startup.campusmate.domain.auth.dto.recovery.FindPasswordRq;
import com.startup.campusmate.domain.auth.dto.session.LoginRq;
import com.startup.campusmate.domain.auth.dto.session.LoginRs;
import com.startup.campusmate.domain.auth.service.AuthService;
import com.startup.campusmate.domain.member.service.MemberService;
import com.startup.campusmate.global.exceptions.GlobalException;
import com.startup.campusmate.global.rsData.RsData;
import com.startup.campusmate.standard.base.Empty;
import com.startup.campusmate.standard.util.Ut;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<RsData<LoginRs>> login(@RequestBody LoginRq loginRq) {
        LoginRs login = authService.login(loginRq.getEmail(), loginRq.getPassword());
        return ResponseEntity.ok(RsData.of("로그인 성공", login));
    }

    @PostMapping("/logout")
    public ResponseEntity<RsData<Empty>> logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("X-REFRESH-TOKEN") String refreshToken
    ) {
        authService.logout(accessToken, refreshToken);
        return ResponseEntity.ok(RsData.of("로그아웃 성공"));
    }

    @PostMapping("/find-id")
    public ResponseEntity<RsData<String>> findMemberId(@RequestBody FindIdRq findIdRq) {
        // 저장소에서 해당 이메일 찾기
        String email = authService.findMemberId(findIdRq.getName(), findIdRq.getPhoneNum());

        if (email == null) throw new GlobalException("이메일 찾기 실패");

        return ResponseEntity.ok(RsData.of("이메일 찾기 성공", email));
    }

    @PostMapping("/find-password")
    public ResponseEntity<RsData<Empty>> findPassword(@RequestBody FindPasswordRq findPasswordRq) {
        //이메일 발송
        try {
            if ( Ut.str.isBlank(findPasswordRq.getEmail()) ) {
                throw new GlobalException("400-1", "이메일 공백은 지원하지 않습니다.");
            }
            if ( Ut.str.isBlank(findPasswordRq.getPhoneNum()) ) {
                throw new GlobalException("400-1", "전화번호 공백은 지원하지 않습니다.");
            }
            authService.sendResetLink(findPasswordRq.getEmail());
            return ResponseEntity.ok(RsData.of("재설정 링크 발송 완료"));
        } catch (MessagingException e) {
            throw new GlobalException("메일 전송 실패");
        }
    }

//    @PostMapping("/verify-phone")
//    public ResponseEntity<RsData<Boolean>> verifyPhone(@RequestBody VerifyPhone verifyPhone) {
//        // 휴대폰 인증하는 코드
//        return RsData.of("인증성공", isVerified);
//    }

}


