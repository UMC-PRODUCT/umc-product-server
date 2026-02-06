package com.umc.product.authentication.adapter.out.external;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Google ID 토큰 검증 Adapter
 * <p>
 * Google OAuth2 tokeninfo endpoint를 사용하여 ID 토큰을 검증합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleIdTokenVerifier {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private final RestClient restClient;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    /**
     * Google ID 토큰을 검증하고 OAuth2Attributes로 변환합니다.
     *
     * @param idToken Google에서 발급받은 ID 토큰
     * @return OAuth2Attributes
     * @throws AuthenticationDomainException 토큰 검증 실패 시
     */
    @Deprecated
    public OAuth2Attributes verifyIdToken(String idToken) {
        log.debug("Google ID Token 검증 시작");

        try {
            // Google tokeninfo endpoint 호출
            GoogleTokenInfoResponse response = restClient.get()
                .uri(GOOGLE_TOKEN_INFO_URL + "?id_token=" + idToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Google tokeninfo 호출 실패: status={}", res.getStatusCode());
                    throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
                })
                .body(GoogleTokenInfoResponse.class);

            if (response == null) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
            }

            // audience(aud) 검증 - 우리 앱의 client ID와 일치해야 함
            if (!googleClientId.equals(response.aud())) {
                log.error("Google ID 토큰 audience 불일치: expected={}, actual={}", googleClientId, response.aud());
                throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
            }

            log.info("Google ID 토큰 검증 성공: sub={}, email={}", response.sub(), response.email());

            // OAuth2Attributes 형식에 맞게 Map 생성
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", response.sub());
            attributes.put("email", response.email());
            attributes.put("name", response.name());
            attributes.put("picture", response.picture());

            return OAuth2Attributes.of("google", attributes);

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google ID 토큰 검증 중 오류 발생", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    public OAuth2Attributes verifyAccessToken(String accessToken) {
        log.debug("Google Access Token 검증 시작");

        try {
            // Google tokeninfo endpoint 호출
            GoogleAccessTokenInfoResponse response = restClient.get()
                .uri(GOOGLE_TOKEN_INFO_URL + "?access_token=" + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Google tokeninfo 호출 실패: status={}", res.getStatusCode());
                    throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_INVALID_ACCESS_TOKEN);
                })
                .body(GoogleAccessTokenInfoResponse.class);

            if (response == null) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
            }

            // audience(aud) 검증 - 우리 앱의 client ID와 일치해야 함
            if (!googleClientId.equals(response.aud())) {
                log.error("Google ID 토큰 audience 불일치: expected={}, actual={}", googleClientId, response.aud());
                throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
            }

            log.info("Google Access Token 검증 성공: sub={}, email={}", response.sub(), response.email());

            // OAuth2Attributes 형식에 맞게 Map 생성
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", response.sub());
            attributes.put("email", response.email());

            return OAuth2Attributes.of("google", attributes);

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google Access Token 검증 중 오류 발생", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    /**
     * Google tokeninfo endpoint 응답 DTO
     */
    private record GoogleTokenInfoResponse(
        String iss,         // issuer
        String azp,         // authorized party
        String aud,         // audience (client ID)
        String sub,         // subject (사용자 고유 ID)
        String email,
        String email_verified,
        String name,
        String picture,
        String given_name,
        String family_name,
        String iat,         // issued at
        String exp          // expiration
    ) {
    }

    private record GoogleAccessTokenInfoResponse(
        String sub,
        String email,
        String aud         // audience (client ID)
    ) {
    }
}
