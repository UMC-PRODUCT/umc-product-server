package com.umc.product.authentication.application.service;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.OAuthTokenLoginResult;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.global.security.MemberPrincipal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class UmcProductOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth 로그인을 시도합니다.");

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        var reg = userRequest.getClientRegistration();

        log.debug("""
                        === OAuth2 ClientRegistration ===
                        registrationId: {}
                        clientId: {}
                        clientName: {}
                        redirectUri: {}
                        scopes: {}
                        authorizationGrantType: {}
                        
                        === ProviderDetails ===
                        authorizationUri: {}
                        tokenUri: {}
                        userInfoUri: {}
                        jwkSetUri: {}
                        issuerUri: {}
                        """,
                reg.getRegistrationId(),
                reg.getClientId(),
                reg.getClientName(),
                reg.getRedirectUri(),
                reg.getScopes(),
                reg.getAuthorizationGrantType(),
                reg.getProviderDetails().getAuthorizationUri(),
                reg.getProviderDetails().getTokenUri(),
                reg.getProviderDetails().getUserInfoEndpoint().getUri(),
                reg.getProviderDetails().getJwkSetUri(),
                reg.getProviderDetails().getIssuerUri()
        );

        log.debug("Access Token: {}\nID Token: {}",
                userRequest.getAccessToken().getTokenValue(),
                userRequest.getAdditionalParameters().get("id_token"));

        // Attributes를 파싱하는 역할은 OAuth2Attributes가 담당
        OAuth2Attributes oAuth2Attributes = OAuth2Attributes.of(
                userRequest.getClientRegistration().getRegistrationId(),
                attributes
        );

        // Request에 OAuth 정보 저장 (FailureHandler에서 사용)
        storeOAuthInfoInRequest(oAuth2Attributes);

        // 공통 비즈니스 로직을 OAuthAuthenticationUseCase에 위임
        OAuthTokenLoginResult result = oAuthAuthenticationUseCase.loginWithOAuth2Attributes(oAuth2Attributes);

        if (result.isExistingMember()) {
            // 가입된 사용자인 경우 MemberPrincipal에 담아서 제공
            return MemberPrincipal.builder()
                    .memberId(result.memberId())
                    .build();
        } else {
            // OAuth 로그인은 성공했지만, 우리에게 가입된 사용자가 아니라면 failure로 들어갈 수 있도록
            // OAuth2Error throw
            log.info("사용자 정보가 없습니다. provider: {}, providerId: {}",
                    oAuth2Attributes.getProvider(),
                    oAuth2Attributes.getProviderId()
            );

            AuthenticationErrorCode NO_MEMBER = AuthenticationErrorCode.OAUTH_SUCCESS_BUT_NO_MEMBER;

            OAuth2Error oauth2Error = new OAuth2Error(
                    NO_MEMBER.getCode(),
                    NO_MEMBER.getMessage(),
                    null
            );

            throw new OAuth2AuthenticationException(oauth2Error, new AuthenticationDomainException(NO_MEMBER));
        }
    }

    private void storeOAuthInfoInRequest(OAuth2Attributes oAuth2Attributes) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.getRequest().setAttribute("oauth_email", oAuth2Attributes.getEmail());
            attributes.getRequest().setAttribute("oauth_provider", oAuth2Attributes.getProvider());
            attributes.getRequest().setAttribute("oauth_provider_id", oAuth2Attributes.getProviderId());
        }
    }
}
