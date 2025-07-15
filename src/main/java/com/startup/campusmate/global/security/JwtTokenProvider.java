package com.startup.campusmate.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${custom.jwt.secret}")
    private String secretKey;

    private final long accessTokenValidity = 1000 * 60 * 30; // 30분
    private final long refreshTokenValidity = 1000 * 60 * 60 * 24 * 7; // 7일

    public String createAccessToken(String email) {
        return createToken(email, accessTokenValidity);
    }

    public String createRefreshToken(String email) {
        return createToken(email, refreshTokenValidity);
    }

    private String createToken(String email, long validityInMillis) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }

    public String getJti(String token) {
        return parseClaims(token).getId();
    }

    public Date getExpiry(String token) {
        return parseClaims(token).getExpiration();
    }

    /** 내부용: 토큰 파싱 후 Claims 반환 */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이라도 Claims 를 반환하고 싶다면
            return e.getClaims();
        }
    }

    public Date getRefreshTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + refreshTokenValidity);
    }
}