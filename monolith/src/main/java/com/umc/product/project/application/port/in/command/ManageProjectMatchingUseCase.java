package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.project.application.port.in.command.dto.ChangeProjectMemberStatusCommand;
import com.umc.product.project.application.port.in.command.dto.RemoveProjectMemberCommand;

/** 운영진 수동 배정·매칭 해제. PM의 보조 PM 관리와 구분한다. */
public interface ManageProjectMatchingUseCase {
    Long assign(AddProjectMemberCommand command);

    void unassign(RemoveProjectMemberCommand command);

    void changeStatus(ChangeProjectMemberStatusCommand command);
}
