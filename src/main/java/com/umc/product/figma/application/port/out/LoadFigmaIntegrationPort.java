package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaIntegration;
import java.util.Optional;

public interface LoadFigmaIntegrationPort {

    /**
     * 단일 위임자만 운영하는 정책상, 가장 최근에 등록된 통합 1건을 반환한다.
     */
    Optional<FigmaIntegration> findActive();

    /**
     * 토큰 갱신 직전 비관적 쓰기 잠금을 획득하며 통합 1건을 반환한다.
     * 동시에 두 스레드가 만료 토큰을 감지해 refresh 엔드포인트를 중복 호출하는 레이스 컨디션을 방지한다.
     */
    Optional<FigmaIntegration> findActiveForUpdate();

    Optional<FigmaIntegration> findByOwnerMemberId(Long ownerMemberId);
}
