package com.startup.campusmate.domain.member.dto.auth.session;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class LoginRq {
    @NonNull
    private String email;
    @NonNull
    private String password;
}
