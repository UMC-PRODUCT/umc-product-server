package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaPartRoute;
import java.util.List;
import java.util.Optional;

public interface LoadFigmaPartRoutePort {

    /**
     * 해당 파일의 전체 매핑 (페이지명 → 라우트, fallback 포함).
     */
    List<FigmaPartRoute> listByFileKey(String fileKey);

    /**
     * 해당 파일에서 매핑되지 않은 댓글이 도달할 fallback 라우트.
     */
    Optional<FigmaPartRoute> findFallbackByFileKey(String fileKey);
}
