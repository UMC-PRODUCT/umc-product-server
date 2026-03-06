package com.umc.product.notification.application.port.out;

import com.umc.product.notification.domain.FcmOutbox;
import java.util.List;

public interface LoadFcmOutboxPort {
    List<FcmOutbox> findPendingEvents();
}
