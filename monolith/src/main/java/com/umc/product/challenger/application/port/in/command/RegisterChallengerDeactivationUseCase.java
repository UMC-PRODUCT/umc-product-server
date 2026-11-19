package com.umc.product.challenger.application.port.in.command;

import com.umc.product.challenger.application.port.in.command.dto.RegisterChallengerDeactivationCommand;

public interface RegisterChallengerDeactivationUseCase {

    Long register(RegisterChallengerDeactivationCommand command);
}
