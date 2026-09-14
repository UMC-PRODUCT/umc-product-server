package com.umc.product.term.application.port.in.command;

import com.umc.product.term.application.port.in.command.dto.CreateTermCommand;

public interface ManageTermUseCase {
    Long createTerms(CreateTermCommand command);
}
