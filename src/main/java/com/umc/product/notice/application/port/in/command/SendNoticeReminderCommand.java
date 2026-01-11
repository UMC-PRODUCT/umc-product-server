package com.umc.product.notice.application.port.in.command;

import java.util.List;

public record SendNoticeReminderCommand(
        Long noticeId,
        Long requesterId,
        List<Long> targetsIds // 리마인드 대상ID
) {
}
