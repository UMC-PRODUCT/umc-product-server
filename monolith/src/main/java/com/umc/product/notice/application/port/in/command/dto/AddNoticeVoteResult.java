package com.umc.product.notice.application.port.in.command.dto;

public record AddNoticeVoteResult(
    Long noticeVoteId,
    Long voteId
) {
}
