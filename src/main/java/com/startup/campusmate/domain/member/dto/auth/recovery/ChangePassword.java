package com.startup.campusmate.domain.member.dto.auth.recovery;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangePassword {
    private String currentPassword;
    private String newPassword;
}
