package com.startup.campusmate.domain.auth.dto.recovery;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindIdRq {
    private String name;
    private String phoneNum;
}