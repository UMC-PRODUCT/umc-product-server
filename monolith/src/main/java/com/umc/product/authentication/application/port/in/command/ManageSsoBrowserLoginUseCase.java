package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.application.port.in.command.dto.LoginSsoBrowserByAppleAuthorizationCodeCommand;
import com.umc.product.authentication.application.port.in.command.dto.LoginSsoBrowserByEmailCommand;
import com.umc.product.authentication.application.port.in.command.dto.LoginSsoBrowserByOAuthTokenCommand;
import com.umc.product.authentication.application.port.in.dto.SsoBrowserLoginInfo;
import com.umc.product.authentication.application.port.in.dto.SsoBrowserOAuthLoginResult;
import com.umc.product.authentication.domain.OAuthAttributes;

public interface ManageSsoBrowserLoginUseCase {

    /** 서버 callback에서 검증된 신원만 입력한다. 미연결 계정에는 가입 증명도 발급하지 않는다. */
    SsoBrowserOAuthLoginResult loginByVerifiedOAuth(
        OAuthAttributes attributes, String appleRefreshToken, String appleClientId);

    SsoBrowserLoginInfo loginByEmail(LoginSsoBrowserByEmailCommand command);

    SsoBrowserOAuthLoginResult loginByOAuthToken(LoginSsoBrowserByOAuthTokenCommand command);

    SsoBrowserOAuthLoginResult loginByAppleAuthorizationCode(LoginSsoBrowserByAppleAuthorizationCodeCommand command);
}
