package com.umc.product.terms.application.port.in.command;

import com.umc.product.terms.application.port.in.command.dto.CreateTermCommand;

public interface ManageTermsUseCase {
    Long createTerms(CreateTermCommand command);
}
