package com.startup.campusmate.domain.token.blacklist.entity;

import com.startup.campusmate.global.jpa.BaseTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BlackListedToken extends BaseTime {
    private String jti;
    private Date expiredData;
}
