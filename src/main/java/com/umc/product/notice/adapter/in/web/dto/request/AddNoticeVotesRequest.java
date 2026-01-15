package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeVotesCommand;
import java.util.List;

public record AddNoticeVotesRequest(
        Long noticeId,
        List<Long> voteIds
) {

    public AddNoticeVotesCommand toCommand() {
        return new AddNoticeVotesCommand(noticeId, voteIds);
    }
}
