package com.startup.campusmate.domain.social.service;

import com.startup.campusmate.domain.auth.service.AuthTokenService;
import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.MemberRepository;
import com.startup.campusmate.domain.social.dto.SocialLoginRq;
import com.startup.campusmate.domain.social.dto.SocialLoginRs;
import com.startup.campusmate.domain.social.dto.SocialUserInfo;
import com.startup.campusmate.domain.social.entity.MemberSocial;
import com.startup.campusmate.domain.social.repository.MemberSocialRepository;
import com.startup.campusmate.global.exceptions.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialService {

    @Value("${custom.app.backend.base-url}")
    private String baseUrl;

    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;
    private final AuthTokenService authTokenService;
    private final GoogleOAuthService googleOAuthService;

    public String getLoginUrl(String provider) {
        return baseUrl + "/oauth2/authorization/" + provider;
    }

    public SocialLoginRs loginWithToken(String provider, SocialLoginRq rq) {
        String codeOrToken = rq.getCode(); // 구글/카카오에서 받은 code 또는 access_token
        SocialUserInfo userInfo;

        if ("google".equals(provider)) {
            String accessToken = googleOAuthService.exchangeGoogleCodeForToken(codeOrToken);
            userInfo = googleOAuthService.getGoogleUserInfo(accessToken);

        } else {
            throw new GlobalException("지원하지 않는 provider입니다.");
        }

        Optional<MemberSocial> socialOpt = memberSocialRepository
                .findByProviderAndProviderId(provider, userInfo.getId());

        Member member;
        if (socialOpt.isPresent()) {
            member = socialOpt.get().getMember();
        } else {
            Optional<Member> memberOpt = memberRepository.findByUsername(userInfo.getEmail());
            member = memberOpt.orElseGet(() -> registerNewSocialMember(userInfo));
            memberSocialRepository.save(
                    MemberSocial.builder()
                            .member(member)
                            .provider(provider)
                            .providerId(userInfo.getId())
                            .build()
            );
        }

        String jwt = authTokenService.createAccessToken(
                member.getUsername(),
                member.getId(),
                member.getAuthorities()
        );

        return new SocialLoginRs(member, jwt);
    }

    public Member registerNewSocialMember(SocialUserInfo userInfo) {
        Member member = Member.builder()
                .username(userInfo.getUsername())
                .nickname(userInfo.getNickname())
                .studentNum(null)
                .build();
        member.setAdmin(false);
        return memberRepository.save(member);
    }

}
