package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.FigmaCommentPreviewInfo;

public interface PreviewFigmaCommentsUseCase {

    /**
     * 특정 watched file 의 신규 댓글을 Discord 발송 없이 read-only 로 조회한다.
     * sync 상태(last_synced_comment_id, last_synced_at) 는 갱신하지 않는다.
     */
    FigmaCommentPreviewInfo preview(Long watchedFileId);
}
