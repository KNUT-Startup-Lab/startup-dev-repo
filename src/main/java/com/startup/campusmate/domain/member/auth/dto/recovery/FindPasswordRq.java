package com.startup.campusmate.domain.member.auth.dto.recovery;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindPasswordRq {
    @NotBlank
    private String username;
    @NotBlank
    private String phoneNum;
}

