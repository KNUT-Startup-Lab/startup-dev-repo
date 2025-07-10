package com.startup.campusmate.domain.user.dto;

import lombok.*;

@Getter
@Builder
public class UserDto {
    @NonNull
    private String email;
    @NonNull
    private String password;
    @NonNull
    private String name;
    @NonNull
    private String phone;
    @NonNull
    private String student_num;
    @NonNull
    private String college;
    @NonNull
    private String major;
}
