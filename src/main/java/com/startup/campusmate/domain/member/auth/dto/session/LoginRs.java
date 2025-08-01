package com.startup.campusmate.domain.member.auth.dto.session;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRs {
    private String username;
    private String accessToken;
    private String refreshToken;
}
