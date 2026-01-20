package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.application.port.in.command.dto.IdTokenLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.IdTokenLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.authentication.application.port.in.command.dto.OAuthLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;

public interface OAuthAuthenticationUseCase {
    /**
     * OAuth 로그인 처리 (회원 조회 또는 신규 가입)
     * <p>
     * 이미 파싱된 OAuth 사용자 정보로 로그인합니다. (웹 Authorization Code Flow용)
     *
     * @param command OAuth 로그인 정보
     * @return 회원 ID
     */
    Long oAuthLogin(OAuthLoginCommand command);

    /**
     * ID 토큰 기반 OAuth 로그인 처리
     * <p>
     * 모바일 클라이언트에서 직접 받은 토큰을 검증하여 로그인합니다. - Google: ID Token 검증 - Kakao: Access Token으로 사용자 정보 조회
     *
     * @param command ID 토큰 로그인 정보
     * @return IdTokenLoginResult (기존 회원 여부, 회원 ID, OAuth 정보 포함)
     */
    IdTokenLoginResult idTokenLogin(IdTokenLoginCommand command);

    /**
     * member에 새로운 OAuth 계정을 연동합니다.
     */
    void linkOAuth(LinkOAuthCommand command);

    /**
     * member에 연동된 OAuth 계정을 해제합니다.
     */
    void unlinkOAuth(UnlinkOAuthCommand command);
}
