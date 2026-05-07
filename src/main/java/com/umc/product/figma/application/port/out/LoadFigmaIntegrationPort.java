package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaIntegration;
import java.util.Optional;

public interface LoadFigmaIntegrationPort {

    /**
     * 단일 위임자만 운영하는 정책상, 가장 최근에 등록된 통합 1건을 반환한다.
     */
    Optional<FigmaIntegration> findActive();

    Optional<FigmaIntegration> findByOwnerMemberId(Long ownerMemberId);
}
