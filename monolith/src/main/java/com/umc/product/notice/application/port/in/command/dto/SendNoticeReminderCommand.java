package com.umc.product.notice.application.port.in.command.dto;

import java.util.List;

public record SendNoticeReminderCommand(
    Long memberId, /* 작성자 ID */
    Long noticeId,
    List<Long> targetIds /* 리마인드 대상ID */
) {

}
