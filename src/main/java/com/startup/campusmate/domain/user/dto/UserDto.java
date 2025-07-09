package com.startup.campusmate.domain.user.dto;

import lombok.Getter;

@Getter
public class UserDto {
    private String email;
    private String password;
    private String name;
    private String phone;
    private String student_num;
    private String college;
    private String major;
}
