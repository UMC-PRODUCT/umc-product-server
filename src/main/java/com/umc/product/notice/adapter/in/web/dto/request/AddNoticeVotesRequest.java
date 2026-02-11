package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeVotesCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AddNoticeVotesRequest(
    @Schema(description = "추가할 투표 ID 목록. 투표 생성 API에서 받은 ID를 전달")
    @NotEmpty(message = "투표 ID 리스트는 비어 있을 수 없습니다.")
    List<Long> voteIds
) {

    public AddNoticeVotesCommand toCommand() {
        return new AddNoticeVotesCommand(voteIds);
    }
}
