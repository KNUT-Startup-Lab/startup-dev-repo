package com.startup.campusmate.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class SignupRq {
    @NonNull
    private String email;

    @NonNull
    private String password;

    private String studentNum;
    private String name;
    private String phoneNum;
    private String college;
    private String major;
}
