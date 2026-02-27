package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.application.port.in.command.dto.AccessTokenLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.authentication.application.port.in.command.dto.OAuthTokenLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;
import java.util.List;

public interface OAuthAuthenticationUseCase {
    /**
     * OAuth2Attributes 기반 로그인 처리 (공통 비즈니스 로직)
     * <p>
     * 웹 플로우와 모바일 플로우 모두 이 메서드를 사용합니다.
     * <p>
     * - 웹: Spring Security OAuth2 → UmcProductOAuth2UserService → 이 메서드
     * <p>
     * - 모바일: Controller → Service → 이 메서드
     */
    OAuthTokenLoginResult loginWithOAuth2Attributes(OAuth2Attributes oAuth2Attributes);

    OAuthTokenLoginResult accessTokenLogin(AccessTokenLoginCommand command);

    /**
     * member에 새로운 OAuth 계정을 연동합니다.
     */
    Long linkOAuth(LinkOAuthCommand command);

    List<Long> linkOAuthBulk(List<LinkOAuthCommand> commands);

    /**
     * member에 연동된 OAuth 계정을 해제합니다.
     */
    void unlinkOAuth(UnlinkOAuthCommand command);
}
