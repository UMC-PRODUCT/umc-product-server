package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "공지 리마인더 발송 요청. 공지를 읽지 않은 사용자에게 푸시 알림을 재발송합니다.")
public record SendNoticeReminderRequest(

    @NotEmpty(message = "리마인드 대상ID 리스트는 비어 있을 수 없습니다.")
    List<Long> targetIds
) {

    public SendNoticeReminderCommand toCommand(Long memberId, Long noticeId) {
        return new SendNoticeReminderCommand(memberId, noticeId, targetIds);
    }
}
