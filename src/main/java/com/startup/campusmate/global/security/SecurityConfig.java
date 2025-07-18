package com.startup.campusmate.global.security;

import com.startup.campusmate.global.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authorizeRequests ->
//                        {
//                            authorizeRequests
//                                    .requestMatchers("/gen/**")
//                                    .permitAll()
//                                    .requestMatchers("/resource/**")
//                                    .permitAll()
//                                    .requestMatchers("/h2-console/**")
//                                    .permitAll();
//
//                            if (AppConfig.isProd()) authorizeRequests
//                                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
//                                    .hasRole("ADMIN");
//
//                            authorizeRequests
//                                    .anyRequest()
//                                    .permitAll();
//                        }
//                )
//                .headers(
//                        headers ->
//                                headers.frameOptions(
//                                        frameOptions ->
//                                                frameOptions.sameOrigin()
//                                )
//                )
//                .csrf(
//                        csrf ->
//                                csrf.disable()
//                )
//                .formLogin(
//                        formLogin ->
//                                formLogin
//                                        .loginPage("/api/login")
//                                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/api/logout") // ← 이렇게 문자열만 쓰면 됨
//                );
//
//        return http.build();
//    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/api", "/api/login",
                                "/api/signup", "/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout.logoutUrl("/api/logout"));
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(new SimpleUrlAuthenticationFailureHandler())
        );
        return http.build();
    }

}




