package com.umc.product.notification.application.port.out;

import com.umc.product.notification.domain.FcmToken;

public interface SaveFcmPort {
    void save(FcmToken newToken);
}
