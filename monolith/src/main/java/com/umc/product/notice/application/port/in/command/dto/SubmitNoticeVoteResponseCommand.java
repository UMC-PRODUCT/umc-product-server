package com.umc.product.notice.application.port.in.command.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record SubmitNoticeVoteResponseCommand(
    Long noticeId,
    Long respondentMemberId,
    List<Long> selectedOptionIds
) {
}
