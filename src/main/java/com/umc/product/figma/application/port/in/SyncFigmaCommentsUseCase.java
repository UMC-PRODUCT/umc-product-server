package com.umc.product.figma.application.port.in;

public interface SyncFigmaCommentsUseCase {

    /**
     * 활성화된 모든 Figma 폴링 대상 파일에 대해 신규 댓글을 수집해 Discord로 전달한다.
     * 스케줄러 / 운영진 수동 트리거 양쪽에서 호출된다.
     */
    void syncAll();

    /**
     * 특정 watched file 한 건만 즉시 동기화한다.
     * 운영진이 on-demand 로 호출하는 경로이며, enabled 여부와 무관하게 동작한다.
     */
    void syncOne(Long watchedFileId);
}
