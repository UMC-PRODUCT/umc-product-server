package com.umc.product.project.application.port.in.command;

import java.util.List;

import com.umc.product.project.application.port.in.command.dto.CreateProjectMatchingRoundCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectMatchingRoundCommand;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;

public interface UpdateProjectMatchingRoundUseCase {

    void update(UpdateProjectMatchingRoundCommand command);

    List<ProjectMatchingRoundInfo> saveAll(List<CreateProjectMatchingRoundCommand> commands);
}
