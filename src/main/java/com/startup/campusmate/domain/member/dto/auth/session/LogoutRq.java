package com.startup.campusmate.domain.member.dto.auth.session;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogoutRq {
    private String email;
    private String password;
}
