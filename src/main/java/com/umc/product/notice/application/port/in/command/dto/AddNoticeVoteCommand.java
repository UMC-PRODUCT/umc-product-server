package com.umc.product.notice.application.port.in.command.dto;

import com.umc.product.notice.domain.NoticeVote;
import java.util.List;

public record AddNoticeVoteCommand(
        Long noticeId,
        Long requesterId,
        List<Long> voteIds
) {
}
