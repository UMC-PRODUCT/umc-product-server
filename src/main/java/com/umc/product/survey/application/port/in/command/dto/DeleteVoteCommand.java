package com.umc.product.survey.application.port.in.command.dto;

public record DeleteVoteCommand(
    Long voteId,
    Long memberId
) {
}
