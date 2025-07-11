package com.startup.campusmate.domain.user.entity;

import com.startup.campusmate.global.jpa.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
public class Admin extends BaseTime {
    @Column(unique = true)
    private String email;

    private String password;
    private String name;
    private String phone;

    @Column(unique = true)
    private String studentNum;

    private String college;
    private String major;

    private String profile_image_url;
}