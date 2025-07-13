package com.startup.campusmate.domain.token.refresh.repository;

import com.startup.campusmate.domain.token.refresh.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByTokenHash(String refreshToken);
}
