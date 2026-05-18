package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.application.port.in.command.dto.AccessTokenLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.AuthorizationCodeLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.authentication.application.port.in.command.dto.OAuthTokenLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;
import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.List;

public interface OAuthAuthenticationUseCase {
    /**
     * OAuth2Attributes 기반 로그인 처리 (공통 비즈니스 로직).
     * <p>
     * 클라이언트가 OAuth 제공자(Google/Kakao/Apple)로부터 직접 받은 access token 또는 authorization code를
     * 토큰 검증 어댑터가 검증·교환하여 사용자 정보를 본 메서드에 전달합니다. access token 흐름과
     * authorization code 흐름 모두 본 메서드를 공통 진입점으로 사용합니다.
     */
    OAuthTokenLoginResult loginWithOAuth2Attributes(OAuth2Attributes oAuth2Attributes);

    OAuthTokenLoginResult accessTokenLogin(AccessTokenLoginCommand command);

    /**
     * Authorization Code 기반 OAuth 로그인 처리.
     * <p>
     * 표준 OAuth2 authorization code grant 흐름을 사용하는 웹/하이브리드 클라이언트가 진입점입니다.
     * 내부적으로 token endpoint 호출 후 access token 흐름과 동일한 공통 로직을 재사용합니다.
     */
    OAuthTokenLoginResult authorizationCodeLogin(AuthorizationCodeLoginCommand command);

    /**
     * member에 새로운 OAuth 계정을 연동합니다.
     */
    Long linkOAuth(LinkOAuthCommand command);

    List<Long> linkOAuthBulk(List<LinkOAuthCommand> commands);

    /**
     * member에 연동된 OAuth 계정을 해제합니다.
     */
    void unlinkOAuth(UnlinkOAuthCommand command);

    /**
     * Apple OAuth의 refresh token과 client_id를 업데이트합니다.
     * <p>
     * Apple은 플랫폼별로 다른 client_id를 사용하기 때문에 refresh token과 함께 발급 시 사용된
     * client_id도 저장하여 추후 revoke 시 동일한 client_id를 사용할 수 있도록 합니다.
     */
    void updateAppleRefreshToken(OAuthProvider provider, String providerId, String refreshToken, String clientId);
}