package com.startup.campusmate.domain.social.repository;

import com.startup.campusmate.domain.social.entity.MemberSocial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSocialRepository extends JpaRepository<MemberSocial, Long> {
    Optional<MemberSocial> findByProviderAndProviderUserId(String provider, String providerId);
}
