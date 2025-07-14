package com.startup.campusmate.domain.member.controller;

import com.startup.campusmate.domain.member.dto.auth.recovery.ChangePassword;
import com.startup.campusmate.domain.member.dto.auth.recovery.FindIdRq;
import com.startup.campusmate.domain.member.dto.auth.recovery.FindPasswordRq;
import com.startup.campusmate.domain.member.dto.auth.session.LoginRq;
import com.startup.campusmate.domain.member.dto.auth.session.LoginRs;
import com.startup.campusmate.domain.member.dto.auth.signup.SignupRq;
import com.startup.campusmate.domain.member.service.MemberService;
import com.startup.campusmate.global.rsData.RsData;
import com.startup.campusmate.standard.base.Empty;
import jakarta.mail.MessagingException;
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
    public RsData<Empty> logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("X-REFRESH-TOKEN") String refreshToken
    ) {
        memberService.logout(accessToken, refreshToken);
        return RsData.of("로그아웃 성공");
    }

    @PostMapping("/find-id")
    public RsData<?> findMemberId(@RequestBody FindIdRq findIdRq) {
        // 저장소에서 해당 이메일 찾기
        String email = memberService.findMemberId(findIdRq.getName(), findIdRq.getPhoneNum());

        if (email != null) {
            return RsData.of("이메일 찾기 성공", email);
        }
        else {
            return RsData.of("이메일 찾기 실패");
        }
    }

    @PostMapping("/find-password")
    public RsData<Empty> findPassword(@RequestBody FindPasswordRq findPasswordRq) {
        //이메일 발송
        try {
            memberService.sendResetLink(findPasswordRq.getEmail());
            return RsData.of("재설정 링크 발송 완료");
        } catch (MessagingException e) {
            return RsData.of("메일 전송 실패");
        }
    }

    @PutMapping("/change-password")
    public RsData<Empty> changePassword(@RequestBody ChangePassword changePassword) {
        // 저장소에서 변경하는 코드
        memberService.changePassword(
                changePassword.getCurrentPassword(),
                changePassword.getNewPassword()
        );
        return RsData.of("비밀번호 변경 성공");
    }

    @GetMapping("/check-email/{email}")
    public RsData<Boolean> checkEmail(@PathVariable("email") String email) {
        Boolean isAvailable = memberService.isEmailAvailable(email);
        return RsData.of("사용 가능한 이메일", isAvailable);
    }

//    @PostMapping("/verify-phone")
//    public RsData<Boolean> verifyPhone(@RequestBody VerifyPhone verifyPhone) {
//        // 휴대폰 인증하는 코드
//        return RsData.of("인증성공", isVerified);
//    }
//
//    @GetMapping("/users/profile")
//    public RsData<MemberDto> profile1() {
//        // 리프레쉬 토큰 엔티티에서 아이디 기반으로 멤버 저장소에서 찾기
//        // Member member = memberRepository.findById(memberId);
//
//        return RsData.of("조회 성공", member);
//    }

//    @PutMapping("/users/profile")
//    public RsData<MemberDto> profile2(@RequestBody MemberDto memberDto) {
//        //DB에서 해당 이메일 기반으로 검색한 다음에 해당 컬럼 수정
//        return RsData.of("수정 성공", memberDto);
//    }

//    @PutMapping("/users/profile/image")
//    public RsData<?> changeImage() {
//        // 고민
//        // 로컬에 있는 파일을 url화 해야 하는데 이걸 api를 만들지
//    }
//
//    @DeleteMapping("/users/profile/image")
//    public RsData<?> deleteImage() {
//        // 패스
//    }

//    @DeleteMapping("/users/account")
//    public RsData<Empty> deleteAccount(String password) {
//        // 비밀번호 비교한 다음 해당 컬럼 삭제
//        return RsData.of("탈퇴 성공");
//    }

}
