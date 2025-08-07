package com.startup.campusmate.domain.member.auth.controller;

import com.startup.campusmate.domain.member.auth.dto.VerifyPhone;
import com.startup.campusmate.domain.member.auth.dto.recovery.FindIdRq;
import com.startup.campusmate.domain.member.auth.dto.recovery.FindPasswordRq;
import com.startup.campusmate.domain.member.auth.dto.session.LoginRq;
import com.startup.campusmate.domain.member.auth.dto.session.LoginRs;
import com.startup.campusmate.domain.member.auth.service.AuthService;
import com.startup.campusmate.global.exceptions.GlobalException;
import com.startup.campusmate.global.rsData.RsData;
import com.startup.campusmate.standard.base.Empty;
import com.startup.campusmate.standard.util.Ut;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<RsData<LoginRs>> login(@RequestBody LoginRq loginRq) {
        LoginRs login = authService.login(loginRq.getUsername(), loginRq.getPassword());
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
    public ResponseEntity<RsData<String>> findMemberUsername(@RequestBody FindIdRq findIdRq) {
        // 저장소에서 해당 이메일 찾기
        String email = authService.findMemberUsername(findIdRq.getNickname(), findIdRq.getPhoneNum());

        if (email == null) throw new GlobalException("이메일 찾기 실패");

        return ResponseEntity.ok(RsData.of("이메일 찾기 성공", email));
    }

    @PostMapping("/find-password")
    public ResponseEntity<RsData<Empty>> findPassword(@RequestBody FindPasswordRq findPasswordRq) {
        //이메일 발송
        try {
            if ( Ut.str.isBlank(findPasswordRq.getUsername()) ) {
                throw new GlobalException("400-1", "이메일 공백은 지원하지 않습니다.");
            }
            if ( Ut.str.isBlank(findPasswordRq.getPhoneNum()) ) {
                throw new GlobalException("400-1", "전화번호 공백은 지원하지 않습니다.");
            }
            authService.sendResetLink(findPasswordRq.getUsername());
            return ResponseEntity.ok(RsData.of("재설정 링크 발송 완료"));
        } catch (MessagingException e) {
            throw new GlobalException("메일 전송 실패");
        }
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<RsData<Boolean>> verifyPhone(@RequestBody VerifyPhone verifyPhone) {
        boolean isVerified = authService.verifyCode(
                verifyPhone.getVerificationCode(),
                verifyPhone.getPhoneNum()
        );

        if (isVerified) {
            return ResponseEntity.ok(RsData.of("S-200", "인증 성공", true));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RsData.of("F-400", "인증 실패", false));
        }
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<RsData<Void>> sendCode(String phoneNum) {
        authService.sendVerificationCode(phoneNum);
        return ResponseEntity.ok(RsData.of("S-200", "인증번호 전송 성공"));
    }

}


