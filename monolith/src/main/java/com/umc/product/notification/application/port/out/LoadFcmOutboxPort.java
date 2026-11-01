package com.umc.product.notification.application.port.out;

import java.util.List;

import com.umc.product.notification.domain.FcmOutbox;

public interface LoadFcmOutboxPort {
    List<FcmOutbox> findPendingEvents();
}
