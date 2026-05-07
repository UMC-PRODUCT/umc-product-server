package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.RegisterFigmaIntegrationCommand;

public interface RegisterFigmaIntegrationUseCase {

    /**
     * Figma OAuth Authorization Code 콜백을 받은 직후, code → token 교환 결과를 영속화한다. 동일 운영진의 재인증인 경우 기존 통합 정보를 덮어쓴다.
     *
     * @return 저장된 통합 ID
     */
    Long register(RegisterFigmaIntegrationCommand command);

    /**
     * 인증된 운영진의 memberId 를 묶어 신규 state 를 발급한다. authorize URL 에 함께 실어 보낸 뒤 콜백에서 {@link #consumeState(String)} 으로
     * 검증/소비한다.
     */
    String issueState(Long ownerMemberId);

    /**
     * state 를 검증함과 동시에 원자적으로 제거하고, 발급 시 묶여 있던 ownerMemberId 를 반환한다. 검증 실패(미발급/만료/이미 사용됨)면 예외를 던진다.
     */
    Long consumeState(String state);
}
