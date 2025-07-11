package com.startup.campusmate.domain.member.entity;

import com.startup.campusmate.global.jpa.BaseTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TokenBlackList extends BaseTime {
    private String Token;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "userId",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_tokenblacklist_user")
    )
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "adminId",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_tokenblacklist_admin")
    )
    private Admin admin;

    private LocalDateTime expiredData;
}
