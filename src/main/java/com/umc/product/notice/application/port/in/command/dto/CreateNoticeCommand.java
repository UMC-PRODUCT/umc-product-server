package com.umc.product.notice.application.port.in.command.dto;

import com.umc.product.notice.dto.NoticeTargetInfo;

public record CreateNoticeCommand(
        Long authorChallengerId,
        String title,
        String content,
        Boolean shouldNotify, /* 알림 발송 여부 */
        NoticeTargetInfo targetInfo
) {
}
