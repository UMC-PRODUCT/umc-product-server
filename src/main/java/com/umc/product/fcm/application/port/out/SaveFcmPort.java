package com.umc.product.fcm.application.port.out;

import com.umc.product.fcm.entity.FCMToken;

public interface SaveFcmPort {
    void save(FCMToken newToken);
}
