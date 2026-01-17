package com.umc.product.notice.application.port.in.command.dto;

import java.util.List;

public record AddNoticeVotesCommand(
        List<Long> voteIds
) {
}
