package com.startup.campusmate.domain.member.service;

import com.startup.campusmate.domain.member.dto.SignupRq;
import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.MemberRepository;
import com.startup.campusmate.domain.social.entity.MemberSocial;
import com.startup.campusmate.domain.social.repository.MemberSocialRepository;
import com.startup.campusmate.global.exceptions.GlobalException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;

    public void signup(SignupRq signupRq) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(signupRq.getUsername())) {
            throw new GlobalException("이미 존재하는 유저이름입니다.");
        }

        // 학번 중복 체크
        if (memberRepository.existsByStudentNum(signupRq.getStudentNum())) {
            throw new GlobalException("이미 존재하는 학번입니다.");
        }

        // 사용자 저장
        Member member = Member.builder()
                .username(signupRq.getUsername())
                .password(passwordEncoder.encode(signupRq.getPassword())) // 실제로는 암호화해야 함
                .nickname(signupRq.getNickname())
                .studentNum(signupRq.getStudentNum())
                .college(signupRq.getCollege())
                .build();

        member.setAdmin(false);
        memberRepository.save(member);
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        // 1) 현재 인증된 유저 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new GlobalException("사용자를 찾을 수 없습니다."));

        // 2) 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new GlobalException("현재 비밀번호가 일치하지 않습니다.");
        }

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public boolean isEmailAvailable(String email) {
        return memberRepository.findByUsername(email).isEmpty();
    }

    public Member modifyOrJoin(String username, String nickname, String provider,String providerId, String profileImageUrl) {
        MemberSocial social = memberSocialRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        Member member = memberRepository.findByUsername(username)
                .orElse(null);

        if (member == null) {
            member = memberRepository.save(Member.builder()
                    .username(username)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .build());

        } else {
            // 이미 등록된 이메일일 경우, 기존 계정에 소셜 연결만
            // 중복 저장 방지
            if (!memberSocialRepository.existsByMemberAndProvider(member, provider)) {
                MemberSocial memberSocial = MemberSocial.builder()
                        .member(member)
                        .provider(provider)
                        .providerId(providerId)
                        .build();
                memberSocialRepository.save(memberSocial);
            }
        }
        return member;
    }
}