package com.umc.product.notice.application.port.in.command;

public record RemoveNoticeImageCommand(
        Long noticeImageId,
        Long requesterId
) {
}
