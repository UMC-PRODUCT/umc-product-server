package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.CreateApplicationCommand;

public interface CreateApplicationUseCase {
    Long create(CreateApplicationCommand command);
}
