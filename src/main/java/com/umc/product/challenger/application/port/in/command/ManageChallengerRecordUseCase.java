package com.umc.product.challenger.application.port.in.command;

import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import java.util.List;

public interface ManageChallengerRecordUseCase {
    void create(CreateChallengerRecordCommand command);

    void createBulk(List<CreateChallengerRecordCommand> commands);

    void delete(Long id);
}
