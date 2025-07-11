package com.startup.campusmate.domain.member.repository.service;

import com.startup.campusmate.domain.member.dto.LoginRsDto;
import com.startup.campusmate.domain.member.dto.MemberDto;
import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.UserRepository;
import com.startup.campusmate.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void signup(MemberDto dto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 학번 중복 체크
        if (userRepository.existsByStudentNum(dto.getStudent_num())) {
            throw new IllegalArgumentException("이미 존재하는 학번입니다.");
        }

        // 사용자 저장
        Member member = Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // 실제로는 암호화해야 함
                .name(dto.getName())
                .phone(dto.getPhone())
                .studentNum(dto.getStudent_num())
                .college(dto.getCollege())
                .major(dto.getMajor())
                .build();

        userRepository.save(member);
    }

    public LoginRsDto login(String email, String password) {
        Member member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }

        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        assert member.getId() != null;
        return LoginRsDto.builder()
                .userId(member.getId())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(String email, String password) {
        Member member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }
        // 토큰 제거// DB에 User 정보 제거
    }
}