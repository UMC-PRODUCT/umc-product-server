package com.umc.product.survey.adapter.in.web.dto.request;

import java.util.List;

public record SubmitVoteResponseRequest(
    List<Long> optionIds
) {
//    public SubmitVoteResponseCommand toCommand(Long voteId, Long memberId) {
//        return new SubmitVoteResponseCommand(voteId, memberId, optionIds);
//    }
//
//    public UpdateVoteResponseCommand toUpdateCommand(Long voteId, Long memberId) {
//        return new UpdateVoteResponseCommand(voteId, memberId, optionIds);
//    }
}
