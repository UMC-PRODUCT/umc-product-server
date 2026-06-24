package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.CreateSeedChallengerRoleCommand;
import com.umc.product.test.application.port.in.command.dto.CreateSeedChallengerRoleResult;

public interface CreateSeedChallengerRoleUseCase {

    CreateSeedChallengerRoleResult create(CreateSeedChallengerRoleCommand command);
}
