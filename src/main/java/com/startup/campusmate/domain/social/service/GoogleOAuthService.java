package com.startup.campusmate.domain.social.service;

import com.startup.campusmate.domain.social.dto.SocialUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoogleOAuthService {
    @Value("${custom.app.base-url}")
    private String baseURL;

    @Value("${custom.google.client-id}")
    private String googleClientId;

    @Value("${custom.google.client-secret}")
    private String googleClientSecret;

    @Value("${custom.google.redirect-uri}")
    private String googleRedirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

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
            String id = (String) body.get("sub");
            String email = (String) body.get("email");
            String name = (String) body.get("name");
            // picture, locale 등도 추가로 파싱 가능

            return new SocialUserInfo(id, email, name);
        } else {
            throw new RuntimeException("Failed to get Google user info: " + response);
        }
    }

}
