package com.startup.campusmate.domain.member.controller;

import com.startup.campusmate.domain.auth.dto.recovery.ChangePassword;
import com.startup.campusmate.domain.auth.dto.signup.SignupRq;
import com.startup.campusmate.domain.member.service.MemberService;
import com.startup.campusmate.global.exceptions.GlobalException;
import com.startup.campusmate.global.rsData.RsData;
import com.startup.campusmate.standard.base.Empty;
import com.startup.campusmate.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("")
    public ResponseEntity<RsData<Empty>> signup(@RequestBody SignupRq signupRq) {
        if ( Ut.str.isBlank(signupRq.getEmail()) ) {
            throw new GlobalException("400-1", "이메일 공백은 지원하지 않습니다.");
        }
        if ( Ut.str.isBlank(signupRq.getStudentNum()) ) {
            throw new GlobalException("400-1", "학번 공백은 지원하지 않습니다.");
        }
        if ( Ut.str.isBlank(signupRq.getPassword()) ) {
            throw new GlobalException("400-1", "비밀번호 공백은 지원하지 않습니다.");
        }
        memberService.signup(signupRq);
        return ResponseEntity.ok(RsData.of("회원가입이 완료되었습니다."));
    }



    @PutMapping("/password")
    public ResponseEntity<RsData<Empty>> changePassword(@RequestBody ChangePassword changePassword) {
        // 저장소에서 변경하는 코드
        memberService.changePassword(
                changePassword.getCurrentPassword(),
                changePassword.getNewPassword()
        );
        return ResponseEntity.ok(RsData.of("비밀번호 변경 성공"));
    }

    @GetMapping("/check-email")
    public ResponseEntity<RsData<Boolean>> checkEmail(@RequestParam("email") String email) {
        Boolean isAvailable = memberService.isEmailAvailable(email);
        return ResponseEntity.ok(RsData.of("사용 가능한 이메일", isAvailable));
    }

//    @GetMapping("/profile")
//    public RsData<MemberDto> profile1() {
//        // 리프레쉬 토큰 엔티티에서 아이디 기반으로 멤버 저장소에서 찾기
//        // Member member = memberRepository.findById(memberId);
//
//        return RsData.of("조회 성공", member);
//    }

//    @PutMapping("/profile")
//    public RsData<MemberDto> profile2(@RequestBody MemberDto memberDto) {
//        //DB에서 해당 이메일 기반으로 검색한 다음에 해당 컬럼 수정
//        return RsData.of("수정 성공", memberDto);
//    }

//    @PutMapping("/profile/image")
//    public RsData<?> changeImage() {
//        // 고민
//        // 로컬에 있는 파일을 url화 해야 하는데 이걸 api를 만들지
//    }
//
//    @DeleteMapping("/profile/image")
//    public RsData<?> deleteImage() {
//        // 패스
//    }

//    @DeleteMapping("/account")
//    public RsData<Empty> deleteAccount(String password) {
//        // 비밀번호 비교한 다음 해당 컬럼 삭제
//        return RsData.of("탈퇴 성공");
//    }

}
