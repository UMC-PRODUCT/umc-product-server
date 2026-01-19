package com.umc.product.notice.application.port.in.command.dto;

import com.umc.product.notice.dto.NoticeTargetInfo;

public record UpdateNoticeCommand(
        Long authorChallengerId,
        Long noticeId,
        String title,
        String content,
        NoticeTargetInfo targetInfo,
        Boolean shouldNotify /* 알림 발송 여부 */
) {
}
