package com.startup.campusmate.domain.member.entity;

import com.startup.campusmate.global.jpa.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class RefreshToken extends BaseTime {
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "userId",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_refresh_token_user")
    )
    private Member member;

    private Date expiredData;
}
