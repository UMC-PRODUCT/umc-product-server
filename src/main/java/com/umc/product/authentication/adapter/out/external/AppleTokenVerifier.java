package com.umc.product.authentication.adapter.out.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Apple Sign-In 토큰 검증 Adapter
 * <p>
 * Apple ID Token(JWT) 서명 검증과 Authorization Code 교환을 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleTokenVerifier {

    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final AppleOAuthProperties appleProperties;
    private final RestClient restClient;
    private PrivateKey cachedPrivateKey;

    /**
     * Apple ID Token(JWT)을 검증하고 OAuth2Attributes로 변환합니다.
     * <p>
     * Apple JWKS 공개키로 서명을 검증하고, issuer/audience를 확인합니다.
     *
     * @param idToken Apple에서 발급받은 identity token (JWT)
     * @return OAuth2Attributes
     * @throws AuthenticationDomainException 토큰 검증 실패 시
     */
    public OAuth2Attributes verifyIdToken(String idToken) {
        log.debug("Apple ID Token 검증 시작");

        try {
            // 1. ID Token의 header에서 kid 추출
            String kid = extractKidFromToken(idToken);

            // 2. Apple JWKS에서 매칭되는 공개키 조회
            PublicKey publicKey = fetchApplePublicKey(kid);

            // 3. JWT 서명 검증 및 claims 파싱
            Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(APPLE_ISSUER)
                .requireAudience(appleProperties.clientId())
                .build()
                .parseSignedClaims(idToken)
                .getPayload();

            String sub = claims.getSubject();
            String email = claims.get("email", String.class);

            log.info("Apple ID Token 검증 성공: sub={}, email={}", sub, email);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", sub);
            attributes.put("email", email);

            return OAuth2Attributes.of("apple", attributes);

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple ID Token 검증 중 오류 발생", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    /**
     * Apple Authorization Code를 교환하여 사용자 정보를 가져옵니다.
     * <p>
     * Authorization Code로 Apple token endpoint에 요청하여 id_token을 받은 뒤, 해당 토큰을 검증합니다.
     *
     * @param authorizationCode Apple에서 발급받은 authorization code
     * @return OAuth2Attributes
     * @throws AuthenticationDomainException 코드 교환 또는 토큰 검증 실패 시
     */
    public OAuth2Attributes verifyAuthorizationCode(String authorizationCode) {
        log.debug("Apple Authorization Code 교환 시작");

        try {
            // 1. client_secret 생성
            String clientSecret = generateClientSecret();

            // 2. Apple token endpoint에 authorization code 교환 요청
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", appleProperties.clientId());
            formData.add("client_secret", clientSecret);
            formData.add("code", authorizationCode);
            formData.add("grant_type", "authorization_code");

            AppleTokenResponse response = restClient.post()
                .uri(APPLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Apple token endpoint 호출 실패: status={}", res.getStatusCode());
                    throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_INVALID_ACCESS_TOKEN);
                })
                .body(AppleTokenResponse.class);

            if (response == null || response.idToken() == null) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
            }

            log.info("Apple Authorization Code 교환 성공, ID Token 검증 진행");

            // 3. 받은 id_token을 검증
            return verifyIdToken(response.idToken());

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple Authorization Code 교환 중 오류 발생", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    /**
     * Apple client_secret JWT를 생성합니다.
     * <p>
     * Apple Developer 문서에 따라 ES256으로 서명된 JWT를 생성합니다.
     *
     * @see <a
     * href="https://developer.apple.com/documentation/accountorganizationaldatasharing/creating-a-client-secret">Creating
     * a client secret</a>
     */
    public String generateClientSecret() {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(3600);

            return Jwts.builder()
                .header()
                .add("kid", appleProperties.keyId())
                .and()
                .issuer(appleProperties.teamId())
                .issuedAt(Date.from(now)).expiration(Date.from(expiration))
                .audience().add(APPLE_ISSUER).and()
                .subject(appleProperties.clientId())
                .signWith(getPrivateKey(), SIG.ES256)
                .compact();

        } catch (Exception e) {
            log.error("Failed to generate Apple client secret", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    /**
     * JWT header에서 kid(Key ID)를 추출합니다.
     */
    private String extractKidFromToken(String token) {
        String header = token.split("\\.")[0];
        byte[] decoded = Base64.getUrlDecoder().decode(header);
        String headerJson = new String(decoded);

        // kid 값 추출 (간단한 JSON 파싱)
        int kidIndex = headerJson.indexOf("\"kid\"");
        if (kidIndex == -1) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
        }

        int valueStart = headerJson.indexOf("\"", headerJson.indexOf(":", kidIndex) + 1) + 1;
        int valueEnd = headerJson.indexOf("\"", valueStart);
        return headerJson.substring(valueStart, valueEnd);
    }

    /**
     * Apple JWKS endpoint에서 kid에 매칭되는 RSA 공개키를 가져옵니다.
     *
     * @see <a
     * href="https://developer.apple.com/documentation/signinwithapplerestapi/fetch-apple-s-public-key-for-verifying-token-signature">Fetch
     * Apple's public key</a>
     */
    private PublicKey fetchApplePublicKey(String kid) {
        AppleJwksResponse jwks = restClient.get()
            .uri(APPLE_JWKS_URL)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                log.error("Apple JWKS 조회 실패: status={}", res.getStatusCode());
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
            })
            .body(AppleJwksResponse.class);

        if (jwks == null || jwks.keys() == null) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }

        AppleJwk matchingKey = jwks.keys().stream()
            .filter(key -> kid.equals(key.kid()))
            .findFirst()
            .orElseThrow(() -> {
                log.error("Apple JWKS에서 kid={}에 매칭되는 키를 찾을 수 없음", kid);
                return new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
            });

        return buildRsaPublicKey(matchingKey);
    }

    /**
     * JWK의 n, e 값으로 RSA PublicKey를 생성합니다.
     */
    private PublicKey buildRsaPublicKey(AppleJwk jwk) {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(jwk.n());
            byte[] eBytes = Base64.getUrlDecoder().decode(jwk.e());

            RSAPublicKeySpec spec = new RSAPublicKeySpec(
                new BigInteger(1, nBytes),
                new BigInteger(1, eBytes)
            );

            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            log.error("Apple RSA 공개키 생성 실패", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    private PrivateKey getPrivateKey() throws Exception {
        if (cachedPrivateKey != null) {
            return cachedPrivateKey;
        }

        // 환경변수에서 \n 리터럴이 들어올 수 있으므로 실제 줄바꿈으로 변환
        String keyStr = appleProperties.privateKey().replace("\\n", "\n");
        log.debug("Private key length: {}", keyStr.length());

        try (StringReader keyReader = new StringReader(keyStr);
             PEMParser pemParser = new PEMParser(keyReader)) {

            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) object;
            cachedPrivateKey = converter.getPrivateKey(privateKeyInfo);

            return cachedPrivateKey;
        }
    }

    // ===== Response DTOs =====

    private record AppleTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("id_token") String idToken
    ) {
    }

    private record AppleJwksResponse(List<AppleJwk> keys) {
    }

    private record AppleJwk(
        String kty,
        String kid,
        String use,
        String alg,
        String n,
        String e
    ) {
    }
}
