package com.startup.campusmate.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupRq {
    @NotBlank
    private String username;
    @NotBlank
    private String studentNum;
    @NotBlank
    private String password;

    private String nickname;
    private String college;
}
