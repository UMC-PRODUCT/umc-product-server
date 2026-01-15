package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import java.util.List;

public record SendNoticeReminderRequest(
        List<Long> targetIds /* 리마인드 대상ID */
) {

    public SendNoticeReminderCommand toCommand(Long noticeId) {
        return new SendNoticeReminderCommand(noticeId, targetIds);
    }
}
