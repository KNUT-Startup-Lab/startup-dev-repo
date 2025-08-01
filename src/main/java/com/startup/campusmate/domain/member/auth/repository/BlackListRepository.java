package com.startup.campusmate.domain.member.auth.repository;

import com.startup.campusmate.domain.member.auth.entity.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListRepository extends JpaRepository<BlackListedToken, Long> {
}
