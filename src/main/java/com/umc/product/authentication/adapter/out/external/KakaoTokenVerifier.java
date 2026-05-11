package com.umc.product.authentication.adapter.out.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Kakao Access Token 검증 Adapter
 * <p>
 * Kakao는 ID 토큰 검증 API를 제공하지 않으므로, Access Token으로 사용자 정보를 조회하여 토큰 유효성을 검증합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTokenVerifier {

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    private final RestClient restClient;
    private final KakaoOAuthProperties kakaoOAuthProperties;

    @Value("${app.oauth2.kakao.admin-key:}")
    private String kakaoAdminKey;

    /**
     * Kakao Authorization Code를 교환하고 OAuth2Attributes로 변환합니다.
     * <p>
     * 동작:
     * <ol>
     *     <li>전달받은 redirect URI가 화이트리스트에 포함되는지 검증한다.</li>
     *     <li>Kakao token endpoint에 grant_type=authorization_code로 POST하여 access token을 교환한다.</li>
     *     <li>받은 access token으로 기존 verifyAccessToken 흐름을 재사용해 사용자 정보를 조회한다.</li>
     * </ol>
     *
     * @param authorizationCode Kakao에서 발급받은 authorization code
     * @param redirectUri       클라이언트가 Kakao 인가 요청에 사용한 redirect URI (화이트리스트와 일치해야 함)
     * @return OAuth2Attributes
     * @throws AuthenticationDomainException redirect URI 불일치, 토큰 교환 실패, 또는 사용자 정보 조회 실패 시
     */
    public OAuth2Attributes verifyAuthorizationCode(String authorizationCode, String redirectUri) {
        log.debug("Kakao Authorization Code 교환 시작");

        if (!kakaoOAuthProperties.isAllowedRedirectUri(redirectUri)) {
            log.warn("허용되지 않은 Kakao redirect URI: {}", redirectUri);
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_REDIRECT_URI);
        }

        String accessToken = exchangeAuthorizationCode(authorizationCode, redirectUri);
        return verifyAccessToken(accessToken);
    }

    /**
     * Kakao token endpoint를 호출하여 authorization code를 access token으로 교환합니다.
     * <p>
     * client_secret은 Kakao 앱이 보안 모드일 때만 필요하며, 비어 있으면 form data에서 제외합니다.
     */
    private String exchangeAuthorizationCode(String authorizationCode, String redirectUri) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoOAuthProperties.clientId());
        formData.add("redirect_uri", redirectUri);
        formData.add("code", authorizationCode);
        if (kakaoOAuthProperties.clientSecret() != null && !kakaoOAuthProperties.clientSecret().isBlank()) {
            formData.add("client_secret", kakaoOAuthProperties.clientSecret());
        }

        try {
            KakaoTokenResponse response = restClient.post()
                .uri(KAKAO_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Kakao 토큰 교환 실패: status={}", res.getStatusCode());
                    throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
                })
                .body(KakaoTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
            }

            log.info("Kakao 토큰 교환 성공");
            return response.accessToken();

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kakao 토큰 교환 중 오류 발생", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    /**
     * Kakao Access Token을 검증하고 OAuth2Attributes로 변환합니다.
     * <p>
     * Kakao API를 호출하여 사용자 정보를 조회함으로써 토큰의 유효성을 검증합니다.
     *
     * @param accessToken Kakao에서 발급받은 Access Token
     * @return OAuth2Attributes
     * @throws AuthenticationDomainException 토큰 검증 실패 시
     */
    public OAuth2Attributes verifyAccessToken(String accessToken) {
        log.debug("Kakao Access Token 검증 시작");

        try {
            // Kakao 사용자 정보 조회 API 호출
            KakaoUserResponse response = restClient.get()
                .uri(KAKAO_USER_INFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Kakao 사용자 정보 조회 실패: status={}", res.getStatusCode());
                    throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_OAUTH_TOKEN);
                })
                .body(KakaoUserResponse.class);

            // 응답을 검증합니다.
            if (response == null || response.id() == null) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
            }

            log.info("Kakao Access Token 검증 성공: id={}, email={}",
                response.id(),
                response.kakaoAccount() != null ? response.kakaoAccount().email() : "N/A"
            );

            // OAuth2Attributes.of("kakao", ...) 형식에 맞게 Map 생성
            Map<String, Object> attributes = buildAttributesMap(response);

            return OAuth2Attributes.of("kakao", attributes);

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kakao Access Token 검증 중 오류 발생", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    /**
     * 카카오에서 온 사용자 정보를 기반으로 정보를 매핑합니다.
     * <p>
     * 카카오에서 응답 온 형식을 그대로 사용합니다.
     */
    private Map<String, Object> buildAttributesMap(KakaoUserResponse response) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", response.id());

        if (response.kakaoAccount() != null) {
            Map<String, Object> kakaoAccount = new HashMap<>();
            kakaoAccount.put("email", response.kakaoAccount().email());

            if (response.kakaoAccount().profile() != null) {
                Map<String, Object> profile = new HashMap<>();
                profile.put("nickname", response.kakaoAccount().profile().nickname());
                kakaoAccount.put("profile", profile);
            }

            attributes.put("kakao_account", kakaoAccount);
        }

        return attributes;
    }

    /**
     * Kakao Access Token을 사용하여 앱과 사용자의 연결을 해제합니다.
     *
     * @param accessToken Kakao Access Token
     * @see <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#unlink">Kakao 연결 끊기</a>
     */
    public void unlinkUser(String accessToken) {
        log.info("Kakao 사용자 연결 끊기 시작");

        try {
            restClient.post()
                .uri(KAKAO_UNLINK_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Kakao 연결 끊기 실패: status={}", res.getStatusCode());
                    throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
                })
                .toBodilessEntity();

            log.info("Kakao 연결 끊기 성공");

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kakao 연결 끊기 중 오류 발생", e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    /**
     * Kakao Admin API를 사용하여 앱과 사용자의 연결을 해제합니다.
     *
     * @param kakaoUserId Kakao 사용자 고유 ID (providerId)
     * @see <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#unlink">Kakao 연결 끊기</a>
     */
    public void unlinkUserByAdmin(String kakaoUserId) {
        log.info("Kakao 사용자 연결 끊기 시작: kakaoUserId={}", kakaoUserId);

        if (kakaoAdminKey == null || kakaoAdminKey.isBlank()) {
            log.warn("Kakao Admin Key가 설정되지 않아 연결 끊기를 skip합니다: kakaoUserId={}", kakaoUserId);
            return;
        }

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("target_id_type", "user_id");
            formData.add("target_id", kakaoUserId);

            restClient.post()
                .uri(KAKAO_UNLINK_URL)
                .header("Authorization", "KakaoAK " + kakaoAdminKey)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Kakao 연결 끊기 실패: status={}, kakaoUserId={}", res.getStatusCode(), kakaoUserId);
                    throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
                })
                .toBodilessEntity();

            log.info("Kakao 연결 끊기 성공: kakaoUserId={}", kakaoUserId);

        } catch (AuthenticationDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kakao 연결 끊기 중 오류 발생: kakaoUserId={}", kakaoUserId, e);
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }

    // ===== Response DTOs =====

    private record KakaoTokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("token_type")
        String tokenType,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("expires_in")
        Integer expiresIn,
        String scope
    ) {
    }

    private record KakaoUserResponse(
        Long id,
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
    ) {
    }

    private record KakaoAccount(
        String email,
        Profile profile
    ) {
    }

    private record Profile(
        String nickname
    ) {
    }
}
