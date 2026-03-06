package com.umc.product.notification.application.port.out;

import com.umc.product.notification.domain.FcmOutbox;

public interface SaveFcmOutboxPort {
    void save(FcmOutbox fcmOutbox);
}
