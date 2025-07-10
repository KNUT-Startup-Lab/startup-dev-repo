package com.startup.campusmate.domain.user.dto;

import lombok.*;

@Getter
public class LoginRqDto {
    @NonNull
    private String email;
    @NonNull
    private String password;
}

