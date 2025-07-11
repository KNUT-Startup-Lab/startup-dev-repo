package com.startup.campusmate.domain.user.controller;

import com.startup.campusmate.domain.user.dto.LoginRsDto;
import com.startup.campusmate.domain.user.dto.UserDto;
import com.startup.campusmate.domain.user.service.UserService;
import com.startup.campusmate.global.rsData.RsData;
import com.startup.campusmate.standard.base.Empty;
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

    @PostMapping("/signup")
    @ResponseBody
    public RsData<Empty> signup(@RequestBody UserDto request) {
        userService.signup(request);
        return RsData.of("%s님, 회원가입이 완료되었습니다.".formatted(request.getName()));
    }

    @PostMapping("/login")
    @ResponseBody
    public RsData<LoginRsDto> login(@RequestBody UserDto request) {
        LoginRsDto response = userService.login(request.getEmail(), request.getPassword());
        return RsData.of("로그인 성공", response);
    }

    @PostMapping("/logout")
    @ResponseBody
    public RsData<Empty> logout(@RequestBody UserDto request) {
        userService.logout(request.getEmail(), request.getPassword());
        return RsData.of("로그아웃 성공");
    }
}
