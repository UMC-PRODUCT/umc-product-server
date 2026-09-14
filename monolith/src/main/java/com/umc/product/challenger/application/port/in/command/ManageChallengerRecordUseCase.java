package com.umc.product.challenger.application.port.in.command;

import java.util.List;

import com.umc.product.challenger.application.port.in.command.dto.ConsumeChallengerRecordCommand;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;

public interface ManageChallengerRecordUseCase {
    Long create(CreateChallengerRecordCommand command);

    List<Long> createBulk(List<CreateChallengerRecordCommand> commands);

    void delete(Long id);

    void consumeCode(ConsumeChallengerRecordCommand command);
}
