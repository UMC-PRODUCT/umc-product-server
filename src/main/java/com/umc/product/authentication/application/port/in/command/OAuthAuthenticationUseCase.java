package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.authentication.application.port.in.command.dto.OAuthLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;

public interface OAuthAuthenticationUseCase {
    /**
     * OAuth 로그인 처리 (회원 조회 또는 신규 가입)
     *
     * @param command OAuth 로그인 정보
     * @return 회원 ID
     */
    Long oAuthLogin(OAuthLoginCommand command);

    /**
     * member에 새로운 OAuth 계정을 연동합니다.
     */
    void linkOAuth(LinkOAuthCommand command);

    /**
     * member에 연동된 OAuth 계정을 해제합니다.
     */
    void unlinkOAuth(UnlinkOAuthCommand command);
}
