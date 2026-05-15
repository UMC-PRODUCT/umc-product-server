package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedMembersCommand;
import jakarta.validation.constraints.PositiveOrZero;

public record SeedMembersRequest(
    @PositiveOrZero
    int idPwCount,
    @PositiveOrZero
    int oauthCount,
    boolean force
) {

    public SeedMembersCommand toCommand() {
        return new SeedMembersCommand(idPwCount, oauthCount, force);
    }
}
