package com.startup.campusmate.domain.member.service;

import com.startup.campusmate.domain.auth.dto.signup.SignupRq;
import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.MemberRepository;
import com.startup.campusmate.global.exceptions.GlobalException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public void signup(SignupRq dto) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new GlobalException("이미 존재하는 이메일입니다.");
        }

        // 학번 중복 체크
        if (memberRepository.existsByStudentNum(dto.getStudentNum())) {
            throw new GlobalException("이미 존재하는 학번입니다.");
        }

        // 사용자 저장
        Member member = Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // 실제로는 암호화해야 함
                .name(dto.getName())
                .phoneNum(dto.getPhoneNum())
                .studentNum(dto.getStudentNum())
                .college(dto.getCollege())
                .major(dto.getMajor())
                .role(dto.getRole())
                .build();

        memberRepository.save(member);
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        // 1) 현재 인증된 유저 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException("사용자를 찾을 수 없습니다."));

        // 2) 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new GlobalException("현재 비밀번호가 일치하지 않습니다.");
        }

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public boolean isEmailAvailable(String email) {
        return memberRepository.findByEmail(email).isEmpty();
    }
}