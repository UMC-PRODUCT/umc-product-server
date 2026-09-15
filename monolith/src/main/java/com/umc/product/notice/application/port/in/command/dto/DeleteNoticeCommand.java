package com.umc.product.notice.application.port.in.command.dto;

public record DeleteNoticeCommand(
    Long memberId,
    Long noticeId
) {
}
