package com.umc.product.notice.adapter.in.web.dto.response.command;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeVoteResult;

public record AddNoticeVoteResponse(
    Long noticeVoteId,
    Long voteId
) {
    public static AddNoticeVoteResponse from(AddNoticeVoteResult result) {
        return new AddNoticeVoteResponse(result.noticeVoteId(), result.voteId());
    }
}
