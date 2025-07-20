package com.startup.campusmate.domain.auth.service;

import com.startup.campusmate.domain.auth.dto.session.LoginRs;
import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.MemberRepository;
import com.startup.campusmate.domain.token.blacklist.entity.BlackListedToken;
import com.startup.campusmate.domain.token.blacklist.repository.BlackListRepository;
import com.startup.campusmate.domain.token.refresh.entity.RefreshToken;
import com.startup.campusmate.domain.token.refresh.repository.RefreshRepository;
import com.startup.campusmate.global.exceptions.GlobalException;
import com.startup.campusmate.global.security.jwt.JwtProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshRepository refreshRepository;
    private final BlackListRepository blacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JavaMailSender mailSender;

    @Value("${custom.app.base-url}")
    private String baseUrl;

    public LoginRs login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException("유저 없음"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new GlobalException("비밀번호 불일치");
        }

        String accessToken = jwtProvider.createAccessToken(email, member.getId(), List.of(new SimpleGrantedAuthority(member.getRole())));
        String refreshToken = jwtProvider.createRefreshToken(email, member.getId(), List.of(new SimpleGrantedAuthority(member.getRole())));
        Date expiredDate = jwtProvider.getRefreshTokenExpiryDate();
        String tokenHash = DigestUtils.sha256Hex(refreshToken);

        RefreshToken token = RefreshToken.builder()
                .tokenHash(tokenHash)
                .member(member)
                .expiredData(expiredDate)
                .build();

        refreshRepository.save(token);

        return LoginRs.builder()
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        refreshRepository.deleteByTokenHash(refreshToken);

        String jti = jwtProvider.getJti(accessToken);
        Date expiredDate = jwtProvider.getExpiry(accessToken);

        BlackListedToken token = BlackListedToken.builder()
                .jti(jti)
                .expiredData(expiredDate)
                .build();
        blacklistRepository.save(token);
    }

    @Transactional
    public void sendResetLink(String email) throws MessagingException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException("등록된 이메일이 아닙니다."));

        String refreshToken = jwtProvider.createRefreshToken(email, member.getId(), List.of(new SimpleGrantedAuthority(member.getRole())));
        Date expiredDate = jwtProvider.getRefreshTokenExpiryDate();
        String tokenHash = DigestUtils.sha256Hex(refreshToken);

        RefreshToken resetToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .member(member)
                .expiredData(expiredDate)
                .build();

        refreshRepository.save(resetToken);

        // 2) 메일 전송
        String resetUrl = baseUrl + "/reset-password?token=" + refreshToken;
        sendEmail(email, resetUrl);
    }

    public String findMemberId(String name, String phoneNum) {
        return memberRepository.findByNameAndPhoneNum(name, phoneNum)
                .map(Member::getEmail)
                .orElseThrow(() -> new GlobalException("핸드폰 번호 불일치"));
    }

    private void sendEmail(String toEmail, String resetUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject("비밀번호 재설정을 위한 링크 안내");

        String content = "<p>안녕하세요.</p>"
                + "<p>아래 링크를 클릭하여 비밀번호를 재설정해 주세요. (유효시간: 1시간)</p>"
                + "<p><a href=\"" + resetUrl + "\">비밀번호 재설정하기</a></p>"
                + "<br><p>감사합니다.</p>";
        helper.setText(content, true);

        mailSender.send(message);
    }
}
