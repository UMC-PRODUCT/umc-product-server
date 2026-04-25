package com.umc.product.notification.application.service;

import com.umc.product.notification.application.port.in.ProcessFcmOutboxUseCase;
import com.umc.product.notification.application.port.out.LoadFcmOutboxPort;
import com.umc.product.notification.application.port.out.SaveFcmOutboxPort;
import com.umc.product.notification.domain.FcmOutbox;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmOutboxService implements ProcessFcmOutboxUseCase {

    private final LoadFcmOutboxPort loadFcmOutboxPort;
    private final SaveFcmOutboxPort saveFcmOutboxPort;

    @Override
    public void process() {
        // 토큰 기반 알림으로 전환됨에 따라 토픽 구독/해제 Outbox 처리 비활성화
        // 기존 PENDING 레코드는 FAILED 처리하여 재처리 루프 방지
        List<FcmOutbox> pendingEvents = loadFcmOutboxPort.findPendingEvents();
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.warn("[DEPRECATED] FCM Outbox 토픽 이벤트 {} 건이 남아있어 FAILED 처리합니다. 토큰 기반 알림으로 전환되었습니다.",
            pendingEvents.size());

        for (FcmOutbox event : pendingEvents) {
            event.markFailed();
            saveFcmOutboxPort.save(event);
        }
    }
}
