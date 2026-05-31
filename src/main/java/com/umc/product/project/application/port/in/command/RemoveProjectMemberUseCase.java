package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.RemoveProjectMemberCommand;

/**
 * 프로젝트 멤버 제거 UseCase (PROJECT-005).
 */
public interface RemoveProjectMemberUseCase {

    void remove(RemoveProjectMemberCommand command);
}
