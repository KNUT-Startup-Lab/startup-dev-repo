package com.startup.campusmate.global.security;

import com.startup.campusmate.domain.member.member.entity.Member;
import com.startup.campusmate.domain.member.member.repository.MemberRepository;
import com.startup.campusmate.domain.member.social.entity.MemberSocial;
import com.startup.campusmate.domain.member.social.repository.MemberSocialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String oauthId = oAuth2User.getName();
        String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = (String) profile.get("nickname");
        String profileImgUrl = (String) profile.get("picture");
        String username = providerTypeCode + "__%s".formatted(oauthId);
        Member member = modifyOrJoin(username, nickname, providerTypeCode, oauthId, profileImgUrl);

        return new SecurityUser(member.getId(), member.getUsername(), member.getPassword(), member.getAuthorities());
    }

    public Member modifyOrJoin(String username, String nickname, String provider,String providerId, String profileImageUrl) {
        MemberSocial social = memberSocialRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        Member member = memberRepository.findByUsername(username)
                .orElse(null);

        if (member == null) {
            member = memberRepository.save(Member.builder()
                    .username(username)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .build());

        } else {
            // 이미 등록된 이메일일 경우, 기존 계정에 소셜 연결만
            // 중복 저장 방지
            if (!memberSocialRepository.existsByMemberAndProvider(member, provider)) {
                MemberSocial memberSocial = MemberSocial.builder()
                        .member(member)
                        .provider(provider)
                        .providerId(providerId)
                        .build();
                memberSocialRepository.save(memberSocial);
            }
        }
        return member;
    }
}
