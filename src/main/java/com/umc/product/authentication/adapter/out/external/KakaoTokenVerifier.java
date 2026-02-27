package com.umc.product.authentication.adapter.out.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
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

    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient;

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

    // ===== Response DTOs =====

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
