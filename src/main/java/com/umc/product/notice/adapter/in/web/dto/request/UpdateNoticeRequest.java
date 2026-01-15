package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import com.umc.product.notice.dto.NoticeTargetInfo;

public record UpdateNoticeRequest(
        String title,
        String content,
        NoticeTargetInfo targetInfo,
        Boolean shouldNotify /* 알림 발송 여부 */
) {

    public UpdateNoticeCommand toCommand(Long noticeId) {
        return new UpdateNoticeCommand(noticeId, title, content, targetInfo, shouldNotify);
    }
}
