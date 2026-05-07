package com.umc.product.figma.application.port.out;

import com.umc.product.figma.application.port.out.dto.FigmaTokenInfo;

public interface FigmaOAuthPort {

    /**
     * authorization code → 신규 token 교환.
     */
    FigmaTokenInfo exchangeCode(String authorizationCode);

    /**
     * refresh token으로 access token 재발급.
     */
    FigmaTokenInfo refresh(String refreshToken);

    /**
     * 운영진을 동의 화면으로 보낼 authorize URL을 생성한다.
     */
    String buildAuthorizeUrl(String state);
}
