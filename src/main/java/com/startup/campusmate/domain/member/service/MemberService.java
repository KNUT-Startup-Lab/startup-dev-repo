package com.startup.campusmate.domain.member.service;

import com.startup.campusmate.domain.member.dto.LoginRs;
import com.startup.campusmate.domain.member.dto.SignupRq;
import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.MemberRepository;
import com.startup.campusmate.domain.token.blacklist.entity.BlackListedToken;
import com.startup.campusmate.domain.token.blacklist.repository.BlackListRepository;
import com.startup.campusmate.domain.token.refresh.entity.RefreshToken;
import com.startup.campusmate.domain.token.refresh.repository.RefreshRepository;
import com.startup.campusmate.global.security.JwtTokenProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshRepository refreshRepository;
    private final BlackListRepository blacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;

    // application.yml에 정의된 도메인 값 가져오기
    @Value("${app.password-reset.base-url}")
    private String resetBaseUrl;

    public void signup(SignupRq dto) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 학번 중복 체크
        if (memberRepository.existsByStudentNum(dto.getStudentNum())) {
            throw new IllegalArgumentException("이미 존재하는 학번입니다.");
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
                .build();

        memberRepository.save(member);
    }

    public LoginRs login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }

        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);
        Date expiredDate = jwtTokenProvider.getRefreshTokenExpiryDate();

        RefreshToken token = RefreshToken.builder()
                .tokenHash(refreshToken)
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

        String jti = jwtTokenProvider.getJti(accessToken);
        Date expiredDate = jwtTokenProvider.getExpiry(accessToken);

        BlackListedToken token = BlackListedToken.builder()
                .jti(jti)
                .expiredData(expiredDate)
                .build();
        blacklistRepository.save(token);
    }

    @Transactional
    public void sendResetLink(String email) throws MessagingException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("등록된 이메일이 아닙니다."));

        String refreshToken = jwtTokenProvider.createRefreshToken(email);
        Date expiredDate = jwtTokenProvider.getRefreshTokenExpiryDate();

        RefreshToken resetToken = RefreshToken.builder()
                .tokenHash(refreshToken)
                .member(member)
                .expiredData(expiredDate)
                .build();

        refreshRepository.save(resetToken);

        // 2) 메일 전송
        String resetUrl = resetBaseUrl + "/reset-password?token=" + refreshToken;
        sendEmail(email, resetUrl);
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        // 1) 현재 인증된 유저 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // 보통 principal이 email이라 가정
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2) 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 3) 새 비밀번호 암호화 후 저장
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
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

    public boolean isEmailAvailable(String email) {
        // 이메일로 조회했을 때 결과가 없으면 사용 가능
        return memberRepository.findByEmail(email).isEmpty();
    }

    public String findMemberId(String name, String phoneNum) {
        return memberRepository.findByNameAndPhoneNum(name, phoneNum)
                .map(Member::getEmail)
                .orElseThrow(() -> new IllegalArgumentException("핸드폰 번호 불일치"));
    }
}