package com.startup.campusmate.domain.token.blacklist.repository;

import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.token.refresh.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);

    void deleteByTokenHash(String refreshToken);
}
