package com.umc.product.notice.application.port.in.command.dto;

public record RemoveNoticeImageCommand(
        Long noticeImageId,
        Long requesterId
) {
}
