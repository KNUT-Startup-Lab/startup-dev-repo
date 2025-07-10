package com.startup.campusmate.domain.user.controller;

import com.startup.campusmate.domain.user.dto.LoginRqDto;
import com.startup.campusmate.domain.user.dto.LoginRsDto;
import com.startup.campusmate.domain.user.dto.UserDto;
import com.startup.campusmate.domain.user.service.AuthService;
import com.startup.campusmate.domain.user.service.UserService;
import com.startup.campusmate.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseBody
    public RsData<UserDto> signup(@RequestBody UserDto requestDto) {
        userService.signup(requestDto);
        return RsData.of("%s님, 회원가입이 완료되었습니다.".formatted(requestDto.getName()), requestDto);
    }

    @PostMapping("/login")
    @ResponseBody
    public RsData<?> login(@RequestBody LoginRqDto request) {
        LoginRsDto response = authService.login(request.getEmail(), request.getPassword());
        return RsData.of("로그인 성공", response);
    }
}
