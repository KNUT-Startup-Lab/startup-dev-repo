package com.startup.campusmate.global.security.oauth2;

import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.MemberRepository;
import com.startup.campusmate.domain.social.entity.MemberSocial;
import com.startup.campusmate.domain.social.repository.MemberSocialRepository;
import com.startup.campusmate.global.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        String provider    = principal.getProvider();
        String providerId  = principal.getProviderId();
        String email       = principal.getEmail();
        String name        = principal.getName();
        Collection<? extends GrantedAuthority> authority = principal.getAuthorities();


        // 2) User ↔ UserSocial 매핑
        MemberSocial social = memberSocialRepository
                .findByProviderAndProviderUserId(provider, providerId)
                .orElse(null);

        Member member;
        if (social != null) {
            member = social.getMember();
        } else {
            member = memberRepository.findByEmail(email)
                    .orElseGet(() -> Member.builder()
                            .email(email)
                            .name(name)
                            .build());
            memberRepository.save(member);

            memberSocialRepository.save(MemberSocial.builder()
                        .member(member)
                        .provider(provider)
                        .providerUserId(providerId)
                        .build());
        }

        // 3) JWT 토큰 생성
        String accessToken  = tokenProvider.createAccessToken(member.getEmail(), member.getId(), authority);
        String refreshToken = tokenProvider.createRefreshToken(member.getEmail(), member.getId(), authority);

        // 4) 응답 헤더 또는 바디에 토큰 전송
        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("Refresh-Token", refreshToken);

        // 필요시 JSON 응답 바디로도 내려줄 수 있습니다.
        response.getWriter().write("{\"accessToken\":\"" + accessToken + "\",\"refreshToken\":\"" + refreshToken + "\"}");
        response.getWriter().flush();

        clearAuthenticationAttributes(request);
    }
}

