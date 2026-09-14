package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeVoteResponseCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UpdateNoticeVoteResponseRequest(

    @Schema(description = "선택한 옵션 ID 목록. 빈 배열로 보내면 기존 응답이 취소됩니다.")
    List<Long> optionIds
) {

    public UpdateNoticeVoteResponseCommand toCommand(Long noticeId, Long respondentMemberId) {
        return UpdateNoticeVoteResponseCommand.builder()
            .noticeId(noticeId)
            .respondentMemberId(respondentMemberId)
            .selectedOptionIds(optionIds)
            .build();
    }
}
