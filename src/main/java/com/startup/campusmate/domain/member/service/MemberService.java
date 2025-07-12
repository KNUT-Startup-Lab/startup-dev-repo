package com.startup.campusmate.domain.member.service;

import com.startup.campusmate.domain.member.dto.LoginRs;
import com.startup.campusmate.domain.member.dto.SignupRq;
import com.startup.campusmate.domain.member.dto.rq.SignupRequest;
import com.startup.campusmate.domain.member.dto.rs.LoginResponse;
import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.token.blacklist.entity.BlackListedToken;
import com.startup.campusmate.domain.token.blacklist.repository.RefreshRepository;
import com.startup.campusmate.domain.token.refresh.entity.RefreshToken;
import com.startup.campusmate.domain.member.repository.UserRepository;
import com.startup.campusmate.domain.token.refresh.repository.BlackListRepository;
import com.startup.campusmate.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;
    private final BlackListRepository blacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void signup(SignupRq dto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 학번 중복 체크
        if (userRepository.existsByStudentNum(dto.getStudentNum()))) {
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

        userRepository.save(member);
    }

    public LoginRs login(String email, String password) {
        Member member = userRepository.findByEmail(email)
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

    public void logout(String accessToken, String refreshToken) {
        refreshRepository.deleteByTokenHash(refreshToken);

        String jti = jwtTokenProvider.getJti(accessToken);
        Date expiredDate = jwtTokenProvider.getExpiry()

        BlackListedToken token = BlackListedToken.builder()
                .jti(jti)
                .expiredData(expiredDate)
                .build();
        blacklistRepository.save(token);
    }
}