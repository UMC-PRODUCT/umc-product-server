package com.umc.product.survey.application.port.in.query.dto;

public record GetVoteDetailsQuery(
    Long voteId,
    Long memberId
) {
}
