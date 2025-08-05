package com.startup.campusmate.domain.member.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDto {
    private String username;
    private String password;
    private String nickname;
    private String studentNum;
    private String college;
    private boolean _isAdmin;
}
