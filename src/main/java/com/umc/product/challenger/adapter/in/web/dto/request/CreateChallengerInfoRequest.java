package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.common.domain.enums.ChallengerPart;

public record CreateChallengerInfoRequest(
        Long memberId,
        ChallengerPart part,
        Long gisuId
) {
    public CreateChallengerCommand toCommand() {
        return new CreateChallengerCommand(memberId, part, gisuId);
    }
}
