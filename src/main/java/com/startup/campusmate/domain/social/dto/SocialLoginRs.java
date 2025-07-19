package com.startup.campusmate.domain.social.dto;


import com.startup.campusmate.domain.member.entity.Member;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SocialLoginRs {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "userId",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_member_social_user")
    )
    private Member member;
    private String jwt;
}
