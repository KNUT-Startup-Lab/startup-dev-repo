package com.startup.campusmate.domain.auth.dto.signup;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupRq {
    @NotBlank
    private String email;
    @NotBlank
    private String studentNum;
    @NotBlank
    private String password;

    private String name;
    private String phoneNum;
    private String college;
    private String major;
    private String role;
}
