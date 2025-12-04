package swyp.dodream.jwt.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성
    public String generateAccessToken(Long userId, String email, String name) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("name", name)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    // 토큰에서 이메일 추출
    public String getEmailFromToken(String token) {
        return getClaims(token).get("email", String.class);
    }

    // 토큰에서 이름 추출
    public String getNameFromToken(String token) {
        return getClaims(token).get("name", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 만료된 토큰인지 확인 (ExpiredJwtException 체크)
    public boolean isTokenExpired(String token) {
        try {
            getClaims(token);
            return false; // 유효한 토큰
        } catch (ExpiredJwtException e) {
            return true; // 만료된 토큰
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 다른 오류 (만료가 아님)
        }
    }

    // 만료된 토큰에서도 정보 추출 (재발급을 위해)
    public Long getUserIdFromExpiredToken(String token) {
        try {
            return getUserIdFromToken(token);
        } catch (ExpiredJwtException e) {
            return Long.parseLong(e.getClaims().getSubject());
        }
    }

    // Access Token 만료 시간을 초 단위로 반환 (쿠키 maxAge용)
    public int getAccessTokenExpirationInSeconds() {
        return (int) (accessTokenExpiration / 1000);
    }

    // Refresh Token 만료 시간을 초 단위로 반환 (쿠키 maxAge용)
    public int getRefreshTokenExpirationInSeconds() {
        return (int) (refreshTokenExpiration / 1000);
    }

    // Claims 추출
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

