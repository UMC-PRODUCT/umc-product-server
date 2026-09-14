package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;

/**
 * 파트별 정원 일괄 갱신 UseCase (PROJECT-105).
 */
public interface UpdatePartQuotasUseCase {

    void update(UpdatePartQuotasCommand command);
}
