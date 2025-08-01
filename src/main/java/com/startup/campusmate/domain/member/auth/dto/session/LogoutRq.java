package com.startup.campusmate.domain.member.auth.dto.session;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogoutRq {
    private String username;
    private String password;
}
