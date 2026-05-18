package com.umc.product.notification.application.port.in.dto;

import com.umc.product.notice.domain.NoticeTargetInfo;
import lombok.Builder;

@Builder
public record AudienceNotificationCommand(
    NoticeTargetInfo targetInfo,
    String title,
    String body
) {
}
