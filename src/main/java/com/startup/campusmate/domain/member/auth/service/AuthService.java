package com.startup.campusmate.domain.member.auth.service;

import com.startup.campusmate.domain.member.auth.dto.session.LoginRs;
import com.startup.campusmate.domain.member.auth.entity.BlackListedToken;
import com.startup.campusmate.domain.member.auth.repository.BlackListRepository;
import com.startup.campusmate.domain.member.member.entity.Member;
import com.startup.campusmate.domain.member.member.repository.MemberRepository;
import com.startup.campusmate.global.exceptions.GlobalException;
import com.startup.campusmate.global.security.JwtTokenProvider;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final BlackListRepository blackListRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMessageService authMessageService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${custom.app.backend.base-url}")
    private String baseUrl;

    public LoginRs login(String username, String password) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new GlobalException("유저 없음"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new GlobalException("비밀번호 불일치");
        }

        String accessToken = jwtTokenProvider.createAccessToken(username, member.getId(), member.getAuthorities());
        String refreshToken = jwtTokenProvider.createRefreshToken(username, member.getId(), member.getAuthorities());
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

        String jti = jwtTokenProvider.getJti(accessToken);
        Date expiredDate = jwtTokenProvider.getExpiry(accessToken);

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

        String refreshToken = jwtTokenProvider.createRefreshToken(email, member.getId(), member.getAuthorities());
        Date expiredDate = jwtTokenProvider.getRefreshTokenExpiryDate();
//        String tokenHash = DigestUtils.sha256Hex(refreshToken);

        member.setRefreshToken(refreshToken);
        memberRepository.save(member);

        // 2) 메일 전송
        String resetUrl = baseUrl + "/reset-password?token=" + refreshToken;
        authMessageService.sendEmail(email, resetUrl);
    }

    public String findMemberUsername(String nickname, String phoneNum) {
        return memberRepository.findByNicknameAndPhoneNum(nickname, phoneNum)
                .map(Member::getUsername)
                .orElseThrow(() -> new GlobalException("핸드폰 번호 불일치"));
    }

    """
    [1] 프론트 → 전화번호 보냄
    [2] 백엔드 → 랜덤 인증번호 생성, Redis 저장
    [3] 백엔드 → smsService로 인증번호 문자 전송 (★★★ 핵심)
    [4] 사용자 → 문자 수신 후 입력
    [5] 프론트 → 전화번호 + 코드 전송
    [6] 백엔드 → Redis 비교 후 검증 성공 여부 반환
    """

    public boolean verifyCode(String phoneNumber, String code) {
        String key = "verify:" + phoneNumber;
        String storedCode = redisTemplate.opsForValue().get(key);
        return code.equals(storedCode);
    }

    public void sendVerificationCode(String phoneNum) {
        String code = String.format("%06d", new Random().nextInt(999999));
        String key = "verify:" + phoneNum;
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
        authMessageService.sendSMS(phoneNum, "인증번호는 " + code + "입니다.");
    }
}
