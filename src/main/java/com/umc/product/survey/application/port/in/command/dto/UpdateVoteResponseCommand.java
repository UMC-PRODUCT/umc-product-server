package com.umc.product.survey.application.port.in.command.dto;

import java.util.List;

public record UpdateVoteResponseCommand(
    Long voteId,
    Long memberId,
    List<Long> optionIds
) {
}
