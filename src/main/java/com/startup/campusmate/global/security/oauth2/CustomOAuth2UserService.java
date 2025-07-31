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
import java.util.Map;

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

        Map<String, Object> kakaoAccount = oauthUser.getAttribute("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        Object id = oauthUser.getAttribute("id");
        System.out.println("ID Type: " + id.getClass());  // ← 실제 타입 Long 확인
        String providerId = id != null ? id.toString() : null;

        String email = kakaoAccount.get("email") != null ? (String) kakaoAccount.get("email") : providerId + "@kakao.com";
        String name = (String) profile.get("nickname");
        String picture = (String) profile.get("profile_image_url");


        // 1) social 매핑 조회
        MemberSocial social = memberSocialRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        Member member = memberRepository.findByEmail(email)
                .orElse(null);

        if (member == null) {
            member = memberRepository.save(Member.builder()
                    .email(email)
                    .name(name)
                    .profileImageUrl(picture)
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

        return new CustomOAuth2User(
                provider,
                providerId,
                email,
                name,
                picture,
                oauthUser.getAttributes(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
