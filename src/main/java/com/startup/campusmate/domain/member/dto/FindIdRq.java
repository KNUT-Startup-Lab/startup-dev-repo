package com.startup.campusmate.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindIdRq {
    private String name;
    private String phoneNum;
}