package com.umc.product.notification.application.port.in;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;
import com.umc.product.notification.application.port.in.dto.TopicNotificationCommand;
import java.util.List;

public interface ManageFcmUseCase {

    void registerFcmToken(Long userId, FcmRegistrationRequest request);

    void sendMessageByToken(NotificationCommand request);

    void subscribeToTopic(List<String> fcmTokens, String topic);

    void unsubscribeFromTopic(List<String> fcmTokens, String topic);

    void sendMessageByTopic(TopicNotificationCommand command);

}
