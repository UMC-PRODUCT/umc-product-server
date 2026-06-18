package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.DeleteSeedProjectDataCommand;

public record DeleteSeedProjectDataRequest(
    Long gisuId
) {

    public DeleteSeedProjectDataCommand toCommand() {
        return DeleteSeedProjectDataCommand.of(gisuId);
    }
}
