package com.umc.product.notice.application.port.in.command.dto;

import java.util.List;

public record SendNoticeReminderCommand(
        Long authorChallengerId,
        Long noticeId,
        List<Long> targetIds /* 리마인드 대상ID */
) {

}
