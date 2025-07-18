package com.startup.campusmate.global.security.oauth2;

import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.MemberRepository;
import com.startup.campusmate.domain.social.entity.MemberSocial;
import com.startup.campusmate.domain.social.repository.MemberSocialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauthUser = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();  // "google"
        String providerId = oauthUser.getAttribute("sub");
        String email      = oauthUser.getAttribute("email");
        String name       = oauthUser.getAttribute("name");

        // 1) social 매핑 조회
        MemberSocial social = memberSocialRepository
                .findByProviderAndProviderUserId(provider, providerId)
                .orElse(null);

        Member member;
        if (social != null) {
            member = social.getMember();
        } else {
            // 2) 이메일 매칭
            member = memberRepository.findByEmail(email)
                    .orElseGet(() -> memberRepository.save(
                            Member.builder()
                            .email(email)
                            .name(name)
                            .build()));

            // 3) UserSocial 매핑 저장
            assert providerId != null;
            MemberSocial memberSocial = MemberSocial.builder()
                    .member(member)
                    .provider(provider)
                    .providerUserId(providerId)
                    .build();
            memberSocialRepository.save(memberSocial);
        }

        return new CustomOAuth2User(
                provider,
                providerId,
                email,
                name,
                oauthUser.getAttributes(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
