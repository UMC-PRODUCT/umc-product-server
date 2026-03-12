package com.umc.product.notice.application.port.in.command;

public interface IncrementNoticeViewCountUseCase {
    void increment(Long noticeId);
}
