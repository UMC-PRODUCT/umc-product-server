package com.umc.product.notification.application.port.out;

public interface SaveFcmTopicPort {

    void saveTopicSubscription(Long fcmTokenId, String topicName);

    void deleteTopicSubscription(Long fcmTokenId, String topicName);

    void deleteAllTopicSubscriptions(Long fcmTokenId);
}
