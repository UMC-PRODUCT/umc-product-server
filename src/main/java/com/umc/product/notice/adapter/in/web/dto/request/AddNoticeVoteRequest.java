package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeVoteCommand;
import java.util.List;

public record AddNoticeVoteRequest(
        Long noticeId,
        List<Long> voteIds
) {

    public AddNoticeVoteCommand toCommand() {
        return new AddNoticeVoteCommand(noticeId, voteIds);
    }
}
