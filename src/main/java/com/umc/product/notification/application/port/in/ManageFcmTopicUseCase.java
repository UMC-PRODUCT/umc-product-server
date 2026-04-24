package com.umc.product.notification.application.port.in;

import java.util.List;

/**
 * FCM 토픽 구독 관리 UseCase
 *
 * @deprecated 토큰 기반 알림(SendNotificationToAudienceUseCase)으로 전환됨. 모든 메서드가 no-op입니다.
 */
@Deprecated(since = "v1.5.0", forRemoval = true)
public interface ManageFcmTopicUseCase {

    @Deprecated(since = "v1.5.0", forRemoval = true)
    void subscribeAllTopicsByMemberId(Long memberId);

    @Deprecated(since = "v1.5.0", forRemoval = true)
    void unsubscribeAllTopicsByMemberId(Long memberId);

    @Deprecated(since = "v1.5.0", forRemoval = true)
    void unsubscribeTokenFromTopics(String fcmToken, Long memberId);

    @Deprecated(since = "v1.5.0", forRemoval = true)
    void unsubscribeLegacyTopics(Long memberId);

    @Deprecated(since = "v1.5.0", forRemoval = true)
    void subscribeToTopic(List<String> fcmTokens, String topic);

    @Deprecated(since = "v1.5.0", forRemoval = true)
    void unsubscribeFromTopic(List<String> fcmTokens, String topic);

    @Deprecated(since = "v1.5.0", forRemoval = true)
    void resubscribeAllLegacyTopics();
}
