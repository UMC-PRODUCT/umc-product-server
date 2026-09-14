package com.umc.product.notice.application.port.in.command.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SubmitNoticeVoteResponseCommand(
    Long noticeId,
    Long respondentMemberId,
    List<Long> selectedOptionIds
) {
}
