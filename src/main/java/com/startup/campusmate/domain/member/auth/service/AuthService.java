package com.startup.campusmate.domain.member.auth.service;

import com.startup.campusmate.domain.member.auth.dto.session.LoginRs;
import com.startup.campusmate.domain.member.member.entity.Member;
import com.startup.campusmate.domain.member.member.repository.MemberRepository;
import com.startup.campusmate.domain.member.auth.entity.BlackListedToken;
import com.startup.campusmate.domain.member.auth.repository.BlackListRepository;
import com.startup.campusmate.global.exceptions.GlobalException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final BlackListRepository blackListRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final SendMailService sendMailService;

    @Value("${custom.app.backend.base-url}")
    private String baseUrl;

    public LoginRs login(String username, String password) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new GlobalException("유저 없음"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new GlobalException("비밀번호 불일치");
        }

        String accessToken = authTokenService.createAccessToken(username, member.getId(), member.getAuthorities());
        String refreshToken = authTokenService.createRefreshToken(username, member.getId(), member.getAuthorities());
//        Date expiredDate = authTokenService.getRefreshTokenExpiryDate();
//        String tokenHash = DigestUtils.sha256Hex(refreshToken);

        member.setRefreshToken(refreshToken);
        memberRepository.save(member);

        return LoginRs.builder()
                .username(member.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        memberRepository.deleteByRefreshToken(refreshToken);

        String jti = authTokenService.getJti(accessToken);
        Date expiredDate = authTokenService.getExpiry(accessToken);

        BlackListedToken token = BlackListedToken.builder()
                .jti(jti)
                .expiredData(expiredDate)
                .build();
        blackListRepository.save(token);
    }

    @Transactional
    public void sendResetLink(String email) throws MessagingException {
        Member member = memberRepository.findByUsername(email)
                .orElseThrow(() -> new GlobalException("등록된 이메일이 아닙니다."));

        String refreshToken = authTokenService.createRefreshToken(email, member.getId(), member.getAuthorities());
        Date expiredDate = authTokenService.getRefreshTokenExpiryDate();
//        String tokenHash = DigestUtils.sha256Hex(refreshToken);

        member.setRefreshToken(refreshToken);
        memberRepository.save(member);

        // 2) 메일 전송
        String resetUrl = baseUrl + "/reset-password?token=" + refreshToken;
        sendMailService.sendEmail(email, resetUrl);
    }

    public String findMemberUsername(String nickname, String phoneNum) {
        return memberRepository.findByNicknameAndPhoneNum(nickname, phoneNum)
                .map(Member::getUsername)
                .orElseThrow(() -> new GlobalException("핸드폰 번호 불일치"));
    }
}
