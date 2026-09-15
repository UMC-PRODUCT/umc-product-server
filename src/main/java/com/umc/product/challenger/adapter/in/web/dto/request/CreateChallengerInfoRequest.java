package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.common.domain.enums.ChallengerPart;

import jakarta.validation.constraints.NotNull;

public record CreateChallengerInfoRequest(
    @NotNull(message = "회원 ID는 필수입니다") Long memberId,
    ChallengerPart part,
    boolean infra,
    @NotNull(message = "기수 ID는 필수입니다") Long gisuId
) {
    public CreateChallengerCommand toCommand() {
        return CreateChallengerCommand.builder()
            .memberId(memberId)
            .part(part)
            .infra(infra)
            .gisuId(gisuId)
            .build();
    }
}
