package com.umc.product.notification.application.port.in;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;

public interface ManageFcmUseCase {

    void registerFcmToken(Long userId, FcmRegistrationRequest request);

    void sendMessageByToken(NotificationCommand request);

}
