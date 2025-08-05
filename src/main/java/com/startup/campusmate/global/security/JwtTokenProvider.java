package com.startup.campusmate.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Component
@Getter
public class JwtTokenProvider {

    private final String secretKey;
    private Key key;
    private final long ACCESS_TOKEN_EXPIRE = 1000 * 60 * 30; // 30분
    private final long REFRESH_TOKEN_EXPIRE = 1000 * 60 * 60 * 24 * 7; // 7일

    // Spring이 Bean을 만들 때
    public JwtTokenProvider(@Value("${custom.jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    // Bean 생성 완료 + 의존성 주입 완료 후
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String username, Long userId, Collection<? extends GrantedAuthority> roles) {
        return createToken(username, userId, roles, ACCESS_TOKEN_EXPIRE);
    }
    public String createRefreshToken(String username, Long userId, Collection<? extends GrantedAuthority> roles) {
        return createToken(username, userId, roles, REFRESH_TOKEN_EXPIRE);
    }

    private String createToken(String username, Long userId, Collection<? extends GrantedAuthority> roles, long validity) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
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

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(this.key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("sub", String.class); // 보통 "sub"에 email/username 저장
    }
}

