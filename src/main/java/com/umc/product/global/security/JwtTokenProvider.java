package com.umc.product.global.security;

import com.umc.product.authentication.domain.EmailVerificationPurpose;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.ClientType;
import com.umc.product.common.domain.enums.OAuthProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth"; // 권한 정보를 저장할 키
    private static final String CLIENT_TYPE_KEY = "clientType"; // 클라이언트 플랫폼(ANDROID/IOS/WEB) 정보를 저장할 키
    private final SecretKey accessTokenSecret;
    private final SecretKey refreshTokenSecret;
    private final SecretKey oAuthVerificationTokenSecret;
    private final SecretKey emailVerificationTokenSecret;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final long verificationTokenValidityInMilliseconds;

    public JwtTokenProvider(
        @Value("${jwt.access-token-secret}") String accessTokenSecret,
        @Value("${jwt.refresh-token-secret}") String refreshTokenSecret,
        @Value("${jwt.oauth-verification-token-secret}") String oAuthVerificationTokenSecret,
        @Value("${jwt.email-verification-token-secret}") String emailVerificationTokenSecret,
        @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
        @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds,
        @Value("${jwt.verification-token-validity-in-seconds}") long verificationTokenValidityInSeconds
    ) {

        // SecretKey 객체로 안전하게 변환
        this.accessTokenSecret = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenSecret = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.oAuthVerificationTokenSecret = Keys.hmacShaKeyFor(oAuthVerificationTokenSecret
            .getBytes(StandardCharsets.UTF_8));
        this.emailVerificationTokenSecret = Keys.hmacShaKeyFor(emailVerificationTokenSecret
            .getBytes(StandardCharsets.UTF_8));

        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
        this.verificationTokenValidityInMilliseconds = verificationTokenValidityInSeconds * 1000;
    }

    /**
     * oAuthVerificationToken 발급 (email, provider, providerId)
     */
    public String createOAuthVerificationToken(String email, OAuthProvider provider, String providerId) {
        Date now = new Date();
        Date validityDate = new Date(now.getTime() + verificationTokenValidityInMilliseconds); // 10분 유효

        return Jwts.builder()
            .subject("OAUTH_VERIFICATION")
            .claim("email", email)
            .claim("provider", provider)
            .claim("providerId", providerId)
            .issuedAt(now)
            .expiration(validityDate)
            .signWith(oAuthVerificationTokenSecret)
            .compact();
    }

    /**
     * emailVerificationToken 발급
     * <p>
     * purpose claim 으로 회원가입(REGISTER) 과 비밀번호 초기화(PASSWORD_RESET) 흐름을 구분한다. 한 흐름에서 발급된 토큰이 다른 흐름에 재사용되지 않도록, 파싱 시
     * expectedPurpose 와 비교한다.
     */
    public String createEmailVerificationToken(String email, EmailVerificationPurpose purpose) {
        Date now = new Date();
        Date validityDate = new Date(now.getTime() + verificationTokenValidityInMilliseconds); // 10분 유효

        return Jwts.builder()
            .subject("EMAIL_VERIFICATION")
            .claim("email", email)
            .claim("purpose", purpose.name())
            .issuedAt(now)
            .expiration(validityDate)
            .signWith(emailVerificationTokenSecret)
            .compact();
    }

    /**
     * AccessToken 생성 메소드
     */
    public String createAccessToken(Long memberId, List<String> roles) {
        return createAccessToken(memberId, roles, (ClientType) null);
    }

    /**
     * AccessToken 생성 메소드 (clientType 포함)
     * <p>
     * clientType 은 트래픽 분포 분석용 optional claim. null 인 경우 claim 자체를 추가하지 않으며, 다운스트림(MDC, 통계)에서는 UNKNOWN 으로 집계된다.
     */
    public String createAccessToken(Long memberId, List<String> roles, ClientType clientType) {
        Date now = new Date();
        Date validityDate = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        var builder = Jwts.builder()
            .subject(String.valueOf(memberId)) // 사용자 식별자 (ID)
            .claim(AUTHORITIES_KEY, roles)     // 권한 정보 저장
            .issuedAt(now)
            .expiration(validityDate)
            .signWith(accessTokenSecret);

        if (clientType != null) {
            builder.claim(CLIENT_TYPE_KEY, clientType.name());
        }

        return builder.compact();
    }

    public String createAccessToken(Long memberId, List<String> roles, Long expiresInSeconds) {
        Date now = new Date();
        Date validityDate = new Date(now.getTime() + expiresInSeconds * 1000);

        return Jwts.builder()
            .subject(String.valueOf(memberId)) // 사용자 식별자 (ID)
            .claim(AUTHORITIES_KEY, roles)     // 권한 정보 저장
            .issuedAt(now)
            .expiration(validityDate)
            .signWith(accessTokenSecret)
            .compact()
            ;
    }

    // 2. Refresh Token 생성
    public String createRefreshToken(Long memberId) {
        // Refresh Token에는 권한 정보 등을 담지 않는 것이 일반적입니다.
        // 필요하다면 roles를 null 대신 넣어도 됩니다.
        Date now = new Date();
        Date validityDate = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
            .subject(String.valueOf(memberId)) // 사용자 식별자 (ID)
            .issuedAt(now)
            .expiration(validityDate)
            .signWith(refreshTokenSecret)
            .compact()
            ;
    }

    public List<String> getRolesFromAccessToken(String token) {
        Claims claims = parseClaims(token, accessTokenSecret);
        Object roles = claims.get(AUTHORITIES_KEY);
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return Collections.emptyList();
    }

    /**
     * AccessToken 에서 clientType claim 을 추출한다.
     * <p>
     * 도입 이전에 발급된 토큰 / clientType 미전달 로그인 경로로 발급된 토큰에는 claim 이 존재하지 않으므로, 그 경우엔 {@code null} 을 반환한다. 호출자는 null-safe 하게
     * 다루어야 하며 통계에서는 "UNKNOWN" 으로 집계한다. 절대 예외를 던지지 않는다.
     */
    public ClientType getClientTypeFromAccessToken(String token) {
        Claims claims = parseClaims(token, accessTokenSecret);
        String clientTypeStr = claims.get(CLIENT_TYPE_KEY, String.class);
        if (clientTypeStr == null) {
            return null;
        }
        try {
            return ClientType.valueOf(clientTypeStr);
        } catch (IllegalArgumentException e) {
            // 알 수 없는 enum 값이 들어와도 통계 집계가 깨지지 않도록 null 처리.
            log.warn("AccessToken 의 clientType claim 값을 해석할 수 없습니다: {}", clientTypeStr);
            return null;
        }
    }

    /**
     * JWT 서명 검증(HMAC-SHA)을 요청당 1회로 한정하여 memberId, roles, clientType을 반환한다. clientType claim이 없거나 알 수 없는 값이면 null을
     * 반환한다.
     */
    @SuppressWarnings("unchecked")
    public ParsedAccessToken parseAndValidateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token, accessTokenSecret);
            Long memberId = Long.parseLong(claims.getSubject());

            Object rolesObj = claims.get(AUTHORITIES_KEY);
            List<String> roles = (rolesObj instanceof List<?>) ? (List<String>) rolesObj : Collections.emptyList();

            String clientTypeStr = claims.get(CLIENT_TYPE_KEY, String.class);
            ClientType clientType = null;
            if (clientTypeStr != null) {
                try {
                    clientType = ClientType.valueOf(clientTypeStr);
                } catch (IllegalArgumentException e) {
                    log.warn("AccessToken 의 clientType claim 값을 해석할 수 없습니다: {}", clientTypeStr);
                }
            }

            return new ParsedAccessToken(memberId, roles, clientType);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
            throw new AuthenticationDomainException(AuthenticationErrorCode.WRONG_JWT_SIGNATURE);
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
            throw new AuthenticationDomainException(AuthenticationErrorCode.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
            throw new AuthenticationDomainException(AuthenticationErrorCode.UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_JWT);
        }
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, accessTokenSecret);
    }

    // 4. 토큰 검증
    private boolean validateToken(String token, SecretKey secretKey) {
        try {
            parseClaims(token, secretKey);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
            throw new AuthenticationDomainException(AuthenticationErrorCode.WRONG_JWT_SIGNATURE);
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
            throw new AuthenticationDomainException(AuthenticationErrorCode.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
            throw new AuthenticationDomainException(AuthenticationErrorCode.UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_JWT);
        }
//        return false;
    }

    // 토큰 파싱 내부 함수
    private Claims parseClaims(String token, SecretKey secretKey) {

        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * AccessToken에서 memberId를 추출하는데 사용됩니다.
     */
    public Long parseAccessToken(String token) {
        return Long.parseLong(parseClaims(token, accessTokenSecret).getSubject());
    }

    /**
     * RefreshToken의 정보를 파싱해서 memberId를 반환합니다.
     */
    public Long parseRefreshToken(String token) {
        validateToken(token, refreshTokenSecret);

        Claims claims = parseClaims(token, refreshTokenSecret);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * oAuthVerificationToken 파싱 및 검증
     */
    public OAuthVerificationClaims parseOAuthVerificationToken(String token) {
        validateToken(token, oAuthVerificationTokenSecret);

        Claims claims = parseClaims(token, oAuthVerificationTokenSecret);

        String email = claims.get("email", String.class);
        String providerStr = claims.get("provider", String.class);
        String providerId = claims.get("providerId", String.class);

        OAuthProvider provider = OAuthProvider.valueOf(providerStr);

        return new OAuthVerificationClaims(email, provider, providerId);
    }

    /**
     * emailVerificationToken 파싱 및 검증
     * <p>
     * 토큰의 purpose claim 이 expectedPurpose 와 일치하지 않으면 INVALID_EMAIL_VERIFICATION 예외를 던진다. 예) 회원가입(REGISTER) 흐름에서 발급된 토큰을
     * 비밀번호 초기화에 사용하려는 cross-purpose 공격 방어.
     */
    public String parseEmailVerificationToken(String token, EmailVerificationPurpose expectedPurpose) {
        validateToken(token, emailVerificationTokenSecret);

        Claims claims = parseClaims(token, emailVerificationTokenSecret);

        String purposeClaim = claims.get("purpose", String.class);
        if (purposeClaim == null || !purposeClaim.equals(expectedPurpose.name())) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
        }

        return claims.get("email", String.class);
    }
}
