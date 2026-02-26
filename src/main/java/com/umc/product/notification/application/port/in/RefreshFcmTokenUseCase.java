package com.umc.product.notification.application.port.in;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;

public interface RefreshFcmTokenUseCase {

    void refreshTokenAndSubscriptions(Long userId, FcmRegistrationRequest request);
}
