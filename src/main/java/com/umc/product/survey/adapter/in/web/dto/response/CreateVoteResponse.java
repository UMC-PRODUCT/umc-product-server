package com.umc.product.survey.adapter.in.web.dto.response;

public record CreateVoteResponse(
    Long voteId
) {
    public static CreateVoteResponse from(Long voteId) {
        return new CreateVoteResponse(voteId);
    }
}
