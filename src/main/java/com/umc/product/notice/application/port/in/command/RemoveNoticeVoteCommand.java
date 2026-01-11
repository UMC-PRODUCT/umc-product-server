package com.umc.product.notice.application.port.in.command;

public record RemoveNoticeVoteCommand(
        Long noticeVoteId,
        Long requesterId
) {
}
