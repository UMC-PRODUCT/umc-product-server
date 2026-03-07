package com.umc.product.notification.application.service;

import com.umc.product.notification.application.port.in.ManageFcmTopicUseCase;
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
    private final ManageFcmTopicUseCase manageFcmTopicUseCase;

    @Override
    public void process() {
        List<FcmOutbox> pendingEvents = loadFcmOutboxPort.findPendingEvents();

        for (FcmOutbox event : pendingEvents) {
            try {
                switch (event.getEventType()) {
                    case FCM_SUBSCRIBE ->
                            manageFcmTopicUseCase.subscribeAllTopicsByMemberId(event.getMemberId());
                    case FCM_UNSUBSCRIBE ->
                            manageFcmTopicUseCase.unsubscribeTokenFromTopics(event.getPayload(), event.getMemberId());
                }
                event.markProcessed();
                log.info("outbox 이벤트 처리 완료 id={}, type={}", event.getId(), event.getEventType());
            } catch (Exception e) {
                event.incrementRetry();
                log.error("outbox 이벤트 처리 실패 id={}, type={}, retryCount={}",
                        event.getId(), event.getEventType(), event.getRetryCount(), e);
            }
            saveFcmOutboxPort.save(event);
        }
    }
}
