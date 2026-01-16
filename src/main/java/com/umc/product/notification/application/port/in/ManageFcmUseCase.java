package com.umc.product.notification.application.port.in;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;

public interface ManageFcmUseCase {

    void registerFcmToken(Long userId, FcmRegistrationRequest request);

    void sendMessageByToken(NotificationCommand request);

}
