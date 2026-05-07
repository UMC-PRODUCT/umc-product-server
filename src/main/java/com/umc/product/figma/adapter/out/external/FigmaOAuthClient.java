package com.umc.product.figma.adapter.out.external;

import com.umc.product.figma.application.port.out.FigmaOAuthPort;
import com.umc.product.figma.application.port.out.dto.FigmaTokenInfo;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class FigmaOAuthClient implements FigmaOAuthPort {

    private final RestClient restClient;
    private final FigmaOAuthProperties properties;

    public FigmaOAuthClient(RestClient restClient, FigmaOAuthProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public FigmaTokenInfo exchangeCode(String authorizationCode) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("redirect_uri", properties.redirectUri());
        form.add("code", authorizationCode);
        form.add("grant_type", "authorization_code");

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                .uri(properties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);
            return toTokenInfo(response, /* fallbackRefreshToken */ null);
        } catch (RestClientResponseException e) {
            log.error("Figma OAuth code 교환 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new FigmaDomainException(FigmaErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED);
        }
    }

    @Override
    public FigmaTokenInfo refresh(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("refresh_token", refreshToken);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                .uri(properties.refreshUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);
            return toTokenInfo(response, refreshToken);
        } catch (RestClientResponseException e) {
            log.error("Figma OAuth refresh 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new FigmaDomainException(FigmaErrorCode.OAUTH_TOKEN_REFRESH_FAILED);
        }
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        return properties.authorizeUri()
            + "?client_id=" + encode(properties.clientId())
            + "&redirect_uri=" + encode(properties.redirectUri())
            + "&scope=" + encode(properties.scope())
            + "&state=" + encode(state)
            + "&response_type=code";
    }

    private FigmaTokenInfo toTokenInfo(Map<String, Object> response, String fallbackRefreshToken) {
        if (response == null) {
            throw new FigmaDomainException(FigmaErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED);
        }
        String accessToken = (String) response.get("access_token");
        String refreshToken = (String) response.getOrDefault("refresh_token", fallbackRefreshToken);
        String scope = (String) response.getOrDefault("scope", properties.scope());
        Number expiresIn = (Number) response.getOrDefault("expires_in", 0);

        if (accessToken == null) {
            throw new FigmaDomainException(FigmaErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED);
        }

        Instant expiresAt = Instant.now().plusSeconds(expiresIn.longValue());
        return new FigmaTokenInfo(accessToken, refreshToken, expiresAt, scope);
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
