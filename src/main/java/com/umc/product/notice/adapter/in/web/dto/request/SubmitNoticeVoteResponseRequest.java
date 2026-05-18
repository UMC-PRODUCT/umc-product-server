package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.SubmitNoticeVoteResponseCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SubmitNoticeVoteResponseRequest(

    @Schema(description = "선택한 옵션 ID 목록. 단일 선택이면 1개, 복수 선택이면 N개.")
    List<Long> optionIds
) {

    public SubmitNoticeVoteResponseCommand toCommand(Long noticeId, Long respondentMemberId) {
        return SubmitNoticeVoteResponseCommand.builder()
            .noticeId(noticeId)
            .respondentMemberId(respondentMemberId)
            .selectedOptionIds(optionIds)
            .build();
    }
}
