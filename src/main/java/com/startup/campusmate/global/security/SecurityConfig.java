package com.startup.campusmate.global.security;

import com.startup.campusmate.global.security.oauth2.CustomOAuth2UserService;
import com.startup.campusmate.global.security.oauth2.CustomOidcUserService;
import com.startup.campusmate.global.security.oauth2.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    private final CustomOidcUserService customOidcUserService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

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
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(new CustomOAuth2FailureHandler())
        );
        return http.build();
    }


    public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {
        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                            AuthenticationException exception) throws IOException {
            // 1. 로그에 남기기
            System.err.println("[OAuth2 로그인 실패]");
            System.err.println("오류 타입: " + exception.getClass().getName());
            System.err.println("오류 메시지: " + exception.getMessage());

            // 2. 콘솔에서 오류 추적
            exception.printStackTrace();

            // 3. 리다이렉트
            String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("/login?error=" + errorMessage);
        }
    }
}




