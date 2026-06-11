package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.CreateSeedChallengerCommand;
import com.umc.product.test.application.port.in.command.dto.CreateSeedChallengerResult;

public interface CreateSeedChallengerUseCase {

    CreateSeedChallengerResult create(CreateSeedChallengerCommand command);
}
