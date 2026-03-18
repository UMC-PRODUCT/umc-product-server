package com.umc.product.survey.adapter.in.web.dto.request;

import com.umc.product.survey.application.port.in.command.dto.SubmitVoteResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateVoteResponseCommand;

import java.util.List;

public record SubmitVoteResponseRequest(
    List<Long> optionIds
) {
    public SubmitVoteResponseCommand toCommand(Long voteId, Long memberId) {
        return new SubmitVoteResponseCommand(voteId, memberId, optionIds);
    }

    public UpdateVoteResponseCommand toUpdateCommand(Long voteId, Long memberId) {
        return new UpdateVoteResponseCommand(voteId, memberId, optionIds);
    }
}
