package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberResult;

public record CreateSeedMemberResponse(
    Long memberId,
    String email
) {

    public static CreateSeedMemberResponse from(CreateSeedMemberResult result) {
        return new CreateSeedMemberResponse(result.memberId(), result.email());
    }
}
