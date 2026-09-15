package com.umc.product.notice.application.port.in.command;

public interface ManageNoticeReadUseCase {
    /*
     * 공지 읽음 처리
     */
    void recordRead(Long noticeId, Long memberId);
}
