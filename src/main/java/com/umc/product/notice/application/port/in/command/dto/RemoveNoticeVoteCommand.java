package com.umc.product.notice.application.port.in.command.dto;

public record RemoveNoticeVoteCommand(
        Long noticeVoteId,
        Long requesterId
) {
}
