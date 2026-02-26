package com.umc.product.survey.adapter.in.web.dto.request;

import com.umc.product.survey.application.port.in.command.dto.SubmitVoteResponseCommand;
import java.util.List;

public record SubmitVoteResponseRequest(
    List<Long> optionIds
) {
    public SubmitVoteResponseCommand toCommand(Long voteId, Long memberId) {
        return new SubmitVoteResponseCommand(voteId, memberId, optionIds);
    }
}
