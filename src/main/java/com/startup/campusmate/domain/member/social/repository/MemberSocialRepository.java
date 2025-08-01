package com.startup.campusmate.domain.member.social.repository;

import com.startup.campusmate.domain.member.member.entity.Member;
import com.startup.campusmate.domain.member.social.entity.MemberSocial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSocialRepository extends JpaRepository<MemberSocial, Long> {
    Optional<MemberSocial> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByMemberAndProvider(Member member, String provider);
}
