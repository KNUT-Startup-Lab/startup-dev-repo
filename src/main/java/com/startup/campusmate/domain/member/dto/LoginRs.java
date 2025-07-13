package com.startup.campusmate.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRs {
    private String email;
    private String accessToken;
    private String refreshToken;
}
