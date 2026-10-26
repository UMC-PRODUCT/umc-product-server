package com.umc.product.project.application.port.in.command;

import java.util.List;

import com.umc.product.project.application.port.in.command.dto.SaveProjectQuotaManagementCommand;

public interface SaveProjectQuotaManagementUseCase {
    List<Long> save(SaveProjectQuotaManagementCommand command);
}
