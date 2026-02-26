package com.umc.product.survey.application.port.in.command.dto;

import java.util.List;

public record SubmitVoteResponseCommand(
    Long voteId,
    Long memberId,
    List<Long> optionIds
) {
}
