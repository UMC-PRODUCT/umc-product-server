package com.umc.product.support.fixture;

import com.umc.product.notification.application.port.out.SaveFcmOutboxPort;
import com.umc.product.notification.domain.FcmOutbox;
import org.springframework.stereotype.Component;

@Component
public class FcmOutboxFixture {

    private final SaveFcmOutboxPort saveFcmOutboxPort;

    public FcmOutboxFixture(SaveFcmOutboxPort saveFcmOutboxPort) {
        this.saveFcmOutboxPort = saveFcmOutboxPort;
    }

    public FcmOutbox 구독_이벤트(Long memberId) {
        FcmOutbox event = FcmOutbox.subscribeEvent(memberId);
        saveFcmOutboxPort.save(event);
        return event;
    }

    public FcmOutbox 구독해제_이벤트(Long memberId, String oldToken) {
        FcmOutbox event = FcmOutbox.unsubscribeEvent(memberId, oldToken);
        saveFcmOutboxPort.save(event);
        return event;
    }
}
