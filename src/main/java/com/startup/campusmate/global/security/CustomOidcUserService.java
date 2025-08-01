package com.startup.campusmate.global.security;

import com.startup.campusmate.domain.member.member.entity.Member;
import com.startup.campusmate.domain.member.member.repository.MemberRepository;
import com.startup.campusmate.domain.member.social.entity.MemberSocial;
import com.startup.campusmate.domain.member.social.repository.MemberSocialRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) {
        // DefaultOidcUser 생성
        OidcUser oidcUser = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = oidcUser.getAttribute("sub");
        String email = oidcUser.getAttribute("email");
        String name = oidcUser.getAttribute("name");
        String picture = oidcUser.getAttribute("picture");

        // 1. 소셜 계정 매핑 여부 확인
        MemberSocial social = memberSocialRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        Member member;
        if (social != null) {
            member = social.getMember();
        } else {
            // 2. 이메일로 기존 회원 탐색 또는 신규 생성
            member = memberRepository.findByUsername(email)
                    .orElseGet(() -> memberRepository.save(
                            Member.builder()
                                    .username(email)
                                    .name(name)
                                    .profileImageUrl(picture)
                                    .build()));

            // 3. MemberSocial 매핑 생성
            MemberSocial memberSocial = MemberSocial.builder()
                    .member(member)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            memberSocialRepository.save(memberSocial);
        }

        // 4. 기본 권한 설정
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // 5. DefaultOidcUser 반환 (권한 + 클레임 + 이름 속성 설정)
        return new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "sub"  // 이름 속성 키
        );
    }
}