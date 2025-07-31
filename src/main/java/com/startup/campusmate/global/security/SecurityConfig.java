package com.startup.campusmate.global.security;

import com.startup.campusmate.global.security.oauth2.CustomOAuth2FailureHandler;
import com.startup.campusmate.global.security.oauth2.CustomOAuth2SuccessHandler;
import com.startup.campusmate.global.security.oauth2.CustomOAuth2UserService;
import com.startup.campusmate.global.security.oauth2.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    private final CustomOidcUserService customOidcUserService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                 "/", "/login/**", "/api/auth/login", "/api/users",
                                "/oauth2/**", "/api/auth/social/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout.logoutUrl("/api/logout"));

        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                        .oidcUserService(customOidcUserService)       // 구글 등 OIDC
                        .userService(customOAuth2UserService)        // 카카오 등 OAuth2
                )
                .successHandler(customOAuth2SuccessHandler)
                .failureHandler(customOAuth2FailureHandler)
        );
        return http.build();
    }



}




