package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaSummaryCursor;
import java.util.Optional;

/**
 * 시간창 기반 figma 동기화의 단일 cursor 를 조회한다 (ADR-004 §Decision 4).
 * cursor 가 아직 부트스트랩되지 않은 환경에서는 빈 Optional 이 반환된다.
 */
public interface LoadFigmaSummaryCursorPort {

    Optional<FigmaSummaryCursor> findCursor();
}
