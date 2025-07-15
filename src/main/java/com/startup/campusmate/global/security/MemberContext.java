package com.startup.campusmate.global.security;

import com.startup.campusmate.domain.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

@Getter
public class MemberContext extends User {

    private final Long id; // Member의 고유 ID
    private final String email; // Member의 이메일
    private final boolean _isAdmin; // 관리자 여부

    public MemberContext(Member member, Collection<? extends GrantedAuthority> authorities) {
        super(member.getEmail(), member.getPassword(), authorities);
        this.id = member.getId();
        this.email = member.getEmail();
        this._isAdmin = member.is_isAdmin();
    }

    // Member 객체로부터 MemberContext를 생성하는 팩토리 메서드
    public static MemberContext fromMember(Member member) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"), // 모든 사용자는 기본적으로 USER 권한을 가집니다.
                member.is_isAdmin() ? new SimpleGrantedAuthority("ROLE_ADMIN") : null // 관리자인 경우 ADMIN 권한을 추가합니다.
        ).stream().filter(a -> a != null).collect(java.util.stream.Collectors.toList());

        return new MemberContext(member, authorities);
    }
}
