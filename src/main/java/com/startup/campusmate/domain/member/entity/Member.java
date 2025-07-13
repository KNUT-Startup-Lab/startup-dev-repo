package com.startup.campusmate.domain.member.entity;

import com.startup.campusmate.global.jpa.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseTime {
    @Column(unique = true)
    private String email;

    private String password;
    private String name;
    private String phoneNum;

    @Column(unique = true)
    private String studentNum;

    private String college;
    private String major;

    private String profile_image_url;

    private boolean _isAdmin;
}
