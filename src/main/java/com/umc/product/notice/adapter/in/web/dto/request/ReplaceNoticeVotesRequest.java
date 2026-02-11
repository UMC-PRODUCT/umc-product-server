package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeVotesCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReplaceNoticeVotesRequest(
    @Schema(description = "교체할 투표 ID 목록. 빈 배열이면 기존 투표 전체 삭제")
    @NotNull(message = "공지 투표 목록은 비어 있을 수 없습니다.")
    List<Long> voteIds
) {
    public ReplaceNoticeVotesCommand toCommand() {
        return new ReplaceNoticeVotesCommand(voteIds);
    }
}
