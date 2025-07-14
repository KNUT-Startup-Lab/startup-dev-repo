package com.startup.campusmate.domain.member.dto.auth.recovery;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindPasswordRq {
    private String email;
    private String phoneNum;
}

