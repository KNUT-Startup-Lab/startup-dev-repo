package com.startup.campusmate.domain.auth.dto.recovery;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindPasswordRq {
    @NotBlank
    private String email;
    @NotBlank
    private String phoneNum;
}

