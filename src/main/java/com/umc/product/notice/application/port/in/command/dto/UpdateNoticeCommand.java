package com.umc.product.notice.application.port.in.command.dto;


public record UpdateNoticeCommand(
    Long memberId,
    Long noticeId,
    String title,
    String content
) {
}
