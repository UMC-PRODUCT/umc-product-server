package com.umc.product.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth"; // 권한 정보를 저장할 키
    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds) {

        // SecretKey 객체로 안전하게 변환
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }

    // 1. Access Token 생성
    public String createAccessToken(Long memberId, List<String> roles) {
        return createToken(memberId, roles, accessTokenValidityInMilliseconds);
    }

    // 2. Refresh Token 생성
    public String createRefreshToken(Long memberId) {
        // Refresh Token에는 권한 정보 등을 담지 않는 것이 일반적입니다.
        // 필요하다면 roles를 null 대신 넣어도 됩니다.
        return createToken(memberId, Collections.emptyList(), refreshTokenValidityInMilliseconds);
    }

    /**
     * AT, RT에서 공용으로 사용하는 JWT 토큰 생성 로직
     */
    private String createToken(Long memberId, List<String> roles, long validity) {
        Date now = new Date();
        Date validityDate = new Date(now.getTime() + validity);

        var builder = Jwts.builder()
                .subject(String.valueOf(memberId)) // 사용자 식별자 (ID)
                .claim(AUTHORITIES_KEY, roles)     // 권한 정보 저장
                .issuedAt(now)
                .expiration(validityDate)
                .signWith(secretKey);

        return builder.compact();
    }

    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public List<String> getRoles(String token) {
        Claims claims = parseClaims(token);
        Object roles = claims.get(AUTHORITIES_KEY);
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return Collections.emptyList();
    }

    // 4. 토큰 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // 토큰 파싱 내부 함수
    private Claims parseClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

//        catch (ExpiredJwtException e) {
//            // 만료된 토큰이어도 클레임(내용)은 필요할 때가 있음 (재발급 시)
//            return e.getClaims();
//        }
    }

    // Access Token 재발급 시 만료된 토큰에서 ID 등을 꺼내기 위한 헬퍼
    public Long getMemberId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }
}