package com.startup.campusmate.User.controller;

import com.startup.campusmate.User.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    @ResponseBody
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.ok().body(Map.of("message", "회원가입 성공"));
    }

}
