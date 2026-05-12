package com.umc.product.figma.application.port.out;

import com.umc.product.figma.domain.FigmaSummaryCursor;

/**
 * 시간창 기반 figma 동기화의 단일 cursor 를 저장한다 (ADR-004 §Decision 4). insert / update 모두 같은 메서드를 통한다. application 코드가 단일 row 불변을
 * 보장한다.
 */
public interface SaveFigmaSummaryCursorPort {

    FigmaSummaryCursor save(FigmaSummaryCursor cursor);
}
