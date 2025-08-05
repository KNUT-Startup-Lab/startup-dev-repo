package com.startup.campusmate.domain.member.auth.dto.recovery;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangePassword {
    private String currentPassword;
    private String newPassword;
}
