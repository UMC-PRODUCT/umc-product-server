package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.DigestFigmaCommentsCommand;
import com.umc.product.figma.application.port.in.dto.FigmaDigestSummary;

public interface DigestFigmaCommentsUseCase {

    /**
     * 활성 watched file 들의 댓글 중 [from, to] 시간창 안의 댓글을 모아 도메인별로 묶어 Discord 로 발송한다. force=true 라 dispatch 행이 있는 댓글도 재발송하며,
     * cursor 는 변경하지 않는다 (ADR-004 §Decision 2).
     */
    FigmaDigestSummary digest(DigestFigmaCommentsCommand command);
}
