package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeVotesCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddNoticeVotesRequest(
        @NotEmpty(message = "투표 ID 리스트는 비어 있을 수 없습니다.")
        List<Long> voteIds
) {

    public AddNoticeVotesCommand toCommand() {
        return new AddNoticeVotesCommand(voteIds);
    }
}
