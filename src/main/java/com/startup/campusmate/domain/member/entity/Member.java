package com.startup.campusmate.domain.member.entity;

import com.startup.campusmate.global.jpa.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseTime {
    @Column(unique = true)
    private String email;

    private String password;
    private String name;

    @Column(unique = true)
    private String studentNum;

    private String phoneNum;
    private String college;
    private String major;
    private String profile_image_url;

    private String role;
}
