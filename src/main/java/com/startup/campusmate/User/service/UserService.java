package com.startup.campusmate.User.service;

import com.startup.campusmate.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void signup(SignupRequestDto dto) {
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
                .password(dto.getPassword()) // 실제로는 암호화해야 함
                .name(dto.getName())
                .phone(dto.getPhone())
                .studentNum(dto.getStudent_num())
                .college(dto.getCollege())
                .major(dto.getMajor())
                .build();

        userRepository.save(user);
    }
}