package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SendNoticeReminderRequest(
        @NotEmpty(message = "리마인드 대상ID 리스트는 비어 있을 수 없습니다.")
        List<Long> targetIds /* 리마인드 대상ID */
) {

    public SendNoticeReminderCommand toCommand(Long noticeId) {
        return new SendNoticeReminderCommand(noticeId, targetIds);
    }
}
