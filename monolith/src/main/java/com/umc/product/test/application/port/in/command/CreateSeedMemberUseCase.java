package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberCommand;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberResult;

public interface CreateSeedMemberUseCase {

    CreateSeedMemberResult create(CreateSeedMemberCommand command);
}
