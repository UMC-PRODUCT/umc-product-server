package com.umc.product.figma.application.port.in;

public interface SyncFigmaCommentsUseCase {

    /**
     * 활성화된 모든 Figma 폴링 대상 파일에 대해 신규 댓글을 수집해 Discord로 전달한다.
     */
    void syncAll();
}
