package com.startup.campusmate.global.security;

import com.startup.campusmate.global.app.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (REST API에서 토큰 기반 인증 시 일반적)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
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

                            // 모든 OPTIONS 요청을 허용 (CORS preflight 요청 처리)
                            authorizeRequests
                                    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                                    .permitAll();

                            authorizeRequests
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
                .formLogin(
                        formLogin ->
                                formLogin
                                        .loginPage("/member/login")
                                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                );

        return http.build();
    }

    // CORS 설정 Bean
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // React 앱이 실행되는 주소 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 자격 증명(쿠키, HTTP 인증) 허용
        configuration.setMaxAge(3600L); // preflight 요청 결과를 캐시할 시간 (초)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정 적용
        return source;
    }
}