package com.umc.product.notification.application.port.in;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;
import com.umc.product.notification.application.port.in.dto.TopicNotificationCommand;

public interface ManageFcmUseCase {

    void registerFcmToken(Long userId, FcmRegistrationRequest request);

    void sendMessageToMember(NotificationCommand request);

    void sendMessageByTopic(TopicNotificationCommand command);

}
