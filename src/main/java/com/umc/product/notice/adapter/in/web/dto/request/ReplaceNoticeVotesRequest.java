package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeVotesCommand;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReplaceNoticeVotesRequest(
    @NotNull(message = "공지 투표 목록은 비어 있을 수 없습니다.")
    List<Long> voteIds
) {
    public ReplaceNoticeVotesCommand toCommand() {
        return new ReplaceNoticeVotesCommand(voteIds);
    }
}
