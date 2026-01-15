package com.umc.product.fcm.application.port.out;

import com.umc.product.fcm.entity.FcmToken;

public interface SaveFcmPort {
    void save(FcmToken newToken);
}
