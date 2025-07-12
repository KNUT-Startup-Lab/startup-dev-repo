package com.startup.campusmate.domain.token.refresh.repository;

import com.startup.campusmate.domain.token.blacklist.entity.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListRepository extends JpaRepository<BlackListedToken, Long> {
    void deleteByTokenHash(String refreshToken);
}
