package com.startup.campusmate.global.security.oauth2;

import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.MemberRepository;
import com.startup.campusmate.domain.social.entity.MemberSocial;
import com.startup.campusmate.domain.social.repository.MemberSocialRepository;
import com.startup.campusmate.global.security.jwt.JwtProvider;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;

    @PostConstruct
    public void init() {
        // 리다이렉트 전략 제거
        this.setRedirectStrategy((request, response, url) -> {});
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String provider = null;
        String providerId = null;
        String email = null;
        String name = null;
        String picture = null;
        Collection<? extends GrantedAuthority> authorities = null;

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomOAuth2User customUser) {
            // 카카오 등 OAuth2 기반 사용자
            provider = customUser.getProvider();
            providerId = customUser.getProviderId();
            email = customUser.getEmail();
            name = customUser.getName();
            picture = customUser.getPicture();
            authorities = customUser.getAuthorities();

        } else if (principal instanceof DefaultOidcUser oidcUser) {
            // 구글 등 OIDC 기반 사용자
            Map<String, Object> attributes = oidcUser.getAttributes();
            authorities = oidcUser.getAuthorities();

            provider = "google"; // 또는 userRequest에서 받아오는 방식으로 처리
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            picture = (String) attributes.get("picture");

        } else {
            throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
        }

        // 2) User ↔ UserSocial 매핑
        MemberSocial social = memberSocialRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        Member member;

        if (social != null) {
            member = social.getMember();
        } else {
            // 이메일로 기존 멤버 탐색
            Optional<Member> existingMember = memberRepository.findByEmail(email);

            if (existingMember.isPresent()) {
                member = existingMember.get();
            } else {
                // 새로 생성 및 저장
                member = Member.builder()
                        .email(email)
                        .name(name)
                        .profileImageUrl(picture)  // 선택사항
                        .build();
                member = memberRepository.save(member);
            }

            // 소셜 계정 매핑 저장
            memberSocialRepository.save(MemberSocial.builder()
                    .member(member)
                    .provider(provider)
                    .providerId(providerId)
                    .build()
            );
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 3) JWT 토큰 생성
        String accessToken  = tokenProvider.createAccessToken(member.getEmail(), member.getId(), authorities);
        String refreshToken = tokenProvider.createRefreshToken(member.getEmail(), member.getId(), authorities);

        // 4) 응답 헤더 또는 바디에 토큰 전송
        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("Refresh-Token", refreshToken);

        // 필요시 JSON 응답 바디로도 내려줄 수 있습니다.
        response.getWriter().write("{\"accessToken\":\"" + accessToken + "\",\"refreshToken\":\"" + refreshToken + "\"}");
        response.getWriter().flush();

        clearAuthenticationAttributes(request);
    }
}

