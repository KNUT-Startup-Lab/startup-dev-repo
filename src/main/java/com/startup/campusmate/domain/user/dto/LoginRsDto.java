package com.startup.campusmate.domain.user.dto;

import lombok.*;

@Getter
@Builder
public class LoginRsDto {
    @NonNull
    private Long userId;
    @NonNull
    private String email;
    @NonNull
    private String accessToken;
    @NonNull
    private String refreshToken;
}
