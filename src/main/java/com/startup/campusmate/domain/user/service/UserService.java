package com.startup.campusmate.domain.user.service;

import com.startup.campusmate.domain.user.dto.UserDto;
import com.startup.campusmate.domain.user.entity.User;
import com.startup.campusmate.domain.user.repository.UserRepository;
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

    public void signup(UserDto dto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 학번 중복 체크
        if (userRepository.existsByStudentNum(dto.getStudent_num())) {
            throw new IllegalArgumentException("이미 존재하는 학번입니다.");
        }

        // 사용자 저장
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // 실제로는 암호화해야 함
                .name(dto.getName())
                .phone(dto.getPhone())
                .studentNum(dto.getStudent_num())
                .college(dto.getCollege())
                .major(dto.getMajor())
                .build();

        userRepository.save(user);
    }
}