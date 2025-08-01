package com.startup.campusmate.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorizeRequests ->
                        {
                            authorizeRequests
                                    .requestMatchers("/")
                                    .permitAll()
                                    .requestMatchers("/login/**")
                                    .permitAll()
                                    .requestMatchers("/api/auth/login")
                                    .permitAll()
                                    .requestMatchers("/api/users")
                                    .permitAll()
                                    .requestMatchers("/oauth2/**")
                                    .permitAll()
                                    .requestMatchers("/api/auth/social/**")
                                    .permitAll();
                            authorizeRequests
                                    .anyRequest().authenticated();
                        }
                )
                .oauth2Login(
                        oauth2Login  ->
                                oauth2Login
                                        .userInfoEndpoint(userInfo -> userInfo
                                                .oidcUserService(customOidcUserService)       // 구글 등 OIDC
                                                .userService(customOAuth2UserService)        // 카카오 등 OAuth2
                    )
                    .successHandler(customOAuth2SuccessHandler)
                    .failureHandler(customOAuth2FailureHandler)
                )
                .formLogin(
                        form ->
                                form
                                        .loginPage("/login")  // 커스텀 로그인 페이지
                                        .loginProcessingUrl("/process-login") // 로그인 요청 URL
                                        .permitAll()
                )
                .logout(
                        logout ->
                                logout
                                        .logoutUrl("/logout")
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(customUserDetailsService)  // 여기 추가
                .passwordEncoder(passwordEncoder())      // 비밀번호 인코딩 필수
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 보통 BCrypt 사용
    }
}




