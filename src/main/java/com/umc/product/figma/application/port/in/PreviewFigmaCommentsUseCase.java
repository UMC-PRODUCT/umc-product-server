package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.FigmaCommentPreviewInfo;

public interface PreviewFigmaCommentsUseCase {

    /**
     * 특정 watched file 의 최근 시간창 댓글을 Discord 발송 없이 read-only 로 조회한다. last_synced_at, dispatch 기록, cursor 는 모두 변경하지 않는다 (ADR-004 §Decision 2).
     */
    FigmaCommentPreviewInfo preview(Long watchedFileId);
}
