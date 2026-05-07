package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.RegisterFigmaIntegrationCommand;

public interface RegisterFigmaIntegrationUseCase {

    /**
     * Figma OAuth Authorization Code 콜백을 받은 직후, code → token 교환 결과를 영속화한다.
     * 동일 운영진의 재인증인 경우 기존 통합 정보를 덮어쓴다.
     *
     * @return 저장된 통합 ID
     */
    Long register(RegisterFigmaIntegrationCommand command);

    /**
     * OAuth state 값을 검증한다. 일치하지 않으면 예외를 던진다.
     */
    void verifyState(String state);

    /**
     * authorize URL로 redirect 시킬 때 사용할 state를 생성하고 보관한다.
     */
    String issueState();
}
