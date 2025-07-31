package com.startup.campusmate.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
