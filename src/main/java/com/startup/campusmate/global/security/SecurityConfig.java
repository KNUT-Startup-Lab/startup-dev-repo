package com.startup.campusmate.global.security;

import com.startup.campusmate.global.app.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        {
                            authorizeRequests
                                    .requestMatchers("/gen/**")
                                    .permitAll()
                                    .requestMatchers("/resource/**")
                                    .permitAll()
                                    .requestMatchers("/h2-console/**")
                                    .permitAll();

                            if (AppConfig.isProd()) authorizeRequests
                                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                    .hasRole("ADMIN");

                            authorizeRequests
                                    .requestMatchers(HttpMethod.GET, "/api/notices", "/api/notices/**").hasAnyRole("USER", "ADMIN") // 공지사항 조회 (목록, 상세, URL)
                                    .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN") // 공지사항 생성
                                    .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasRole("ADMIN") // 공지사항 수정
                                    .requestMatchers(HttpMethod.DELETE, "/api/notices/**").hasRole("ADMIN") // 공지사항 삭제
                                    .requestMatchers(HttpMethod.POST, "/api/notices/attachments").hasRole("ADMIN") // 첨부파일 업로드
                                    .requestMatchers(HttpMethod.GET, "/api/notices/attachments/**").hasAnyRole("USER", "ADMIN") // 첨부파일 다운로드
                                    .anyRequest()
                                    .permitAll();
                        }
                )
                .headers(
                        headers ->
                                headers.frameOptions(
                                        frameOptions ->
                                                frameOptions.sameOrigin()
                                )
                )
                .csrf(
                        csrf ->
                                csrf.disable()
                )
                .formLogin(
                        formLogin ->
                                formLogin
                                        .loginPage("/member/login")
                                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/member/logout") // ← 이렇게 문자열만 쓰면 됨
                );

        return http.build();
    }
}