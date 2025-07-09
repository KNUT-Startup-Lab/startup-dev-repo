package com.startup.campusmate.domain.user.controller;

import com.startup.campusmate.domain.user.dto.UserDto;
import com.startup.campusmate.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @ResponseBody
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.ok().body(Map.of("message", "회원가입 성공"));
    }

}
