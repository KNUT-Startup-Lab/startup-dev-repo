package com.startup.campusmate.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRsDto {
    private Long userId;
    private String email;
    private String accessToken;
    private String refreshToken;
}
