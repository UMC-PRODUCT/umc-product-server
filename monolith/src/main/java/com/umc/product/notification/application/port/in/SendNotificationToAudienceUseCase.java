package com.umc.product.notification.application.port.in;

import com.umc.product.notification.application.port.in.dto.AudienceNotificationCommand;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;
import java.util.List;

public interface SendNotificationToAudienceUseCase {

    /**
     * NoticeTargetInfo 기반 대상자를 조회하여 각 멤버의 활성 FCM 토큰으로 알림을 발송합니다. 토픽 기반 sendMessageByTopic()을 대체합니다.
     */
    void sendToAudience(AudienceNotificationCommand command);

    /**
     * 단일 멤버의 모든 활성 FCM 토큰으로 알림을 발송합니다.
     */
    void sendToMember(NotificationCommand command);

    /**
     * 여러 멤버의 활성 FCM 토큰을 한 번에 조회하여 FCM MulticastMessage 배치로 발송합니다. remindNotice 등 다수 멤버에게 동일 알림을 보낼 때 사용합니다.
     */
    void sendToMembers(List<Long> memberIds, String title, String body);

}
