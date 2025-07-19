package com.startup.campusmate.domain.social.service;

import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.MemberRepository;
import com.startup.campusmate.domain.social.dto.SocialLoginRq;
import com.startup.campusmate.domain.social.dto.SocialLoginRs;
import com.startup.campusmate.domain.social.dto.SocialUserInfo;
import com.startup.campusmate.domain.social.entity.MemberSocial;
import com.startup.campusmate.domain.social.repository.MemberSocialRepository;
import com.startup.campusmate.global.exceptions.GlobalException;
import com.startup.campusmate.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialService {

    @Value("${app.base-url}")
    private String baseURL;

    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;
    private final JwtProvider jwtProvider;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getLoginUrl(String provider) {
        return baseURL + "/oauth2/authorization/" + provider;
    }

    public String exchangeGoogleCodeForToken(String code) {
        String tokenEndpoint = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenEndpoint, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> body = response.getBody();
            // access_token이 key로 들어있음
            return (String) body.get("access_token");
        } else {
            throw new RuntimeException("Failed to exchange code for token: " + response);
        }
    }

    public SocialUserInfo getGoogleUserInfo(String accessToken) {
        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // Authorization: Bearer {token}

        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoEndpoint,
                HttpMethod.GET,
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> body = response.getBody();
            String id = (String) body.get("sub");      // 구글 고유 ID
            String email = (String) body.get("email");
            String name = (String) body.get("name");
            // 필요하다면 picture, locale 등도 추가로 파싱 가능

            return new SocialUserInfo(id, email, name);
        } else {
            throw new RuntimeException("Failed to get Google user info: " + response);
        }
    }

    public SocialLoginRs loginWithToken(String provider, SocialLoginRq rq) {
        String codeOrToken = rq.getCode(); // 구글/카카오에서 받은 code 또는 access_token

        // 1. 소셜 서버에 토큰 교환 or 유저 정보 요청
        SocialUserInfo userInfo;
        if ("google".equals(provider)) {
            String accessToken = exchangeGoogleCodeForToken(codeOrToken);
            userInfo = getGoogleUserInfo(accessToken);

        } else {
            throw new GlobalException("지원하지 않는 provider입니다.");
        }

        Optional<MemberSocial> socialOpt = memberSocialRepository.findByProviderAndProviderUserId(provider, userInfo.getId());

        Member member;
        if (socialOpt.isPresent()) {
            member = socialOpt.get().getMember();
        } else {
            Optional<Member> memberOpt = memberRepository.findByEmail(userInfo.getEmail());
            if (memberOpt.isPresent()) {
                member = memberOpt.get();
            } else {
                member = registerNewSocialMember(userInfo, provider);
            }
            // 소셜 연동 저장
            memberSocialRepository.save(
                    MemberSocial.builder()
                            .member(member)
                            .provider(provider)
                            .providerUserId(userInfo.getId())
                            .build()
            );
        }

        String jwt = jwtProvider.createAccessToken(
                member.getEmail(),
                member.getId(),
                List.of(new SimpleGrantedAuthority(member.getRole()))
        );

        return new SocialLoginRs(member, jwt);
    }

    public Member registerNewSocialMember(SocialUserInfo userInfo) {
        Member member = Member.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .studentNum(null)
                .role("ROLE_USER")   // 기본 권한
                .build();

        return memberRepository.save(member);
    }

}
