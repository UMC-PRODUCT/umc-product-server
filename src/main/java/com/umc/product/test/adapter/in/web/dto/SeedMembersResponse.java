package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedMembersResult;

public record SeedMembersResponse(
    int registered,
    boolean skipped,
    String reason
) {

    public static SeedMembersResponse from(SeedMembersResult result) {
        return new SeedMembersResponse(
            result.registered(),
            result.skipped(),
            result.reason()
        );
    }
}
