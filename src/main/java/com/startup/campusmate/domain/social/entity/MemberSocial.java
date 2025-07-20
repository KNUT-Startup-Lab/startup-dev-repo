package com.startup.campusmate.domain.social.entity;

import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
public class MemberSocial extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "userId",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_member_social_user")
    )
    private Member member;
    private String provider;
    private String providerUserId;
}
