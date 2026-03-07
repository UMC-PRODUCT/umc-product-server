package com.umc.product.notification.adapter.out.persistentce;

import com.umc.product.notification.application.port.out.LoadFcmOutboxPort;
import com.umc.product.notification.application.port.out.SaveFcmOutboxPort;
import com.umc.product.notification.domain.FcmOutbox;
import com.umc.product.notification.domain.FcmOutboxStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FcmOutboxPersistenceAdapter implements SaveFcmOutboxPort, LoadFcmOutboxPort {

    private final FcmOutboxJpaRepository fcmOutboxJpaRepository;

    @Override
    public void save(FcmOutbox fcmOutbox) {
        fcmOutboxJpaRepository.save(fcmOutbox);
    }

    @Override
    public List<FcmOutbox> findPendingEvents() {
        return fcmOutboxJpaRepository.findByStatus(FcmOutboxStatus.PENDING);
    }
}
