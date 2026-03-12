package com.umc.product.notice.application.service.command;

import com.umc.product.notice.application.port.in.command.IncrementNoticeViewCountUseCase;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeViewCountService implements IncrementNoticeViewCountUseCase {

    private final SaveNoticePort saveNoticePort;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increment(Long noticeId) {
        saveNoticePort.incrementViewCount(noticeId);
    }
}
