package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.DigestFigmaCommentsCommand;
import com.umc.product.figma.application.port.in.dto.FigmaDigestSummary;

public interface DigestFigmaCommentsUseCase {

    /**
     * 활성 watched file 들의 댓글 중 [from, to] 시간창 안의 댓글을 모아 도메인별로 묶어 Discord 로 발송한다.
     * sync 상태(last_synced_comment_id) 는 변경하지 않는다.
     */
    FigmaDigestSummary digest(DigestFigmaCommentsCommand command);
}
