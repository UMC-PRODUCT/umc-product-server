package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateChapterCommand;

public interface ManageChapterUseCase {

    Long create(CreateChapterCommand command);
}
