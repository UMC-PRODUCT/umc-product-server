package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.RegisterFigmaPartRouteCommand;

public interface ManageFigmaPartRouteUseCase {

    /**
     * 페이지 이름 → 파트 role + Discord webhook 매핑을 등록한다.
     * fileKey 와 pageName 의 조합이 이미 존재하면 충돌 예외.
     */
    Long register(RegisterFigmaPartRouteCommand command);

    void delete(Long routeId);
}
