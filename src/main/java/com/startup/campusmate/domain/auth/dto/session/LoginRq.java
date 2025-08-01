package com.startup.campusmate.domain.auth.dto.session;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class LoginRq {
    @NonNull
    private String username;
    @NonNull
    private String password;
}
