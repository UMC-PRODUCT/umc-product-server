package com.umc.product.challenger.application.port.in.command;

import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import java.util.List;

public interface ManageChallengerRecordUseCase {
    Long create(CreateChallengerRecordCommand command);

    List<Long> createBulk(List<CreateChallengerRecordCommand> commands);

    void delete(Long id);
}
