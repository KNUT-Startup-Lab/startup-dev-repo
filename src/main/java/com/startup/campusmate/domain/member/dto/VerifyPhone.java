package com.startup.campusmate.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyPhone {
    private String phoneNum;
    private String verificationCode;
}
