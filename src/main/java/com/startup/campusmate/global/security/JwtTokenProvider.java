package com.startup.campusmate.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private final Key key;
    private final long ACCESS_TOKEN_EXPIRE = 1000 * 60 * 30; // 30분
    private final long REFRESH_TOKEN_EXPIRE = 1000 * 60 * 60 * 24 * 7; // 7일


    public JwtTokenProvider(@Value("${custom.jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String email, String userId, String roles) {
        return createToken(email, userId, roles, ACCESS_TOKEN_EXPIRE);
    }
    public String createRefreshToken(String email, String userId, String roles) {
        return createToken(email, userId, roles, REFRESH_TOKEN_EXPIRE);
    }

    private String createToken(String email, String userId, String roles, long validity) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .setId(UUID.randomUUID().toString())
                .claim("userId", userId)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false; // 만료
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 변조/에러
        }
    }

    public String getJti(String token) {
        return getClaims(token).getId();
    }

    public Date getExpiry(String token) {
        return getClaims(token).getExpiration();
    }

    /** 내부용: 토큰 파싱 후 Claims 반환 */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이라도 Claims를 반환
            return e.getClaims();
        }
    }

    public Date getRefreshTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE);
    }

}
