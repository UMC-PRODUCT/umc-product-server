package com.umc.product.fcm.application.port.in;

import com.umc.product.fcm.adapter.in.web.dto.request.FcmRegistrationRequest;

public interface ManageFcmUseCase {

    void registerFCMToken(Long userId, FcmRegistrationRequest request);

    void sendMessageByToken(NotificationCommand request);

}
