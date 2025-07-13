package com.startup.campusmate.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDto {
    private String email;
    private String password;
    private String name;
    private String phoneNum;
    private String studentNum;
    private String college;
    private String major;
    private boolean _isAdmin;
}
