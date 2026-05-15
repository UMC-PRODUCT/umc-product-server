package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedMembersResult;

public record SeedMembersResponse(
    int registeredIdPw,
    int registeredOAuth,
    boolean skipped,
    String reason
) {

    public static SeedMembersResponse from(SeedMembersResult result) {
        return new SeedMembersResponse(
            result.registeredIdPw(),
            result.registeredOAuth(),
            result.skipped(),
            result.reason()
        );
    }
}
