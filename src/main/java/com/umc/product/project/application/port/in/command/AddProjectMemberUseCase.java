package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;

/**
 * 프로젝트 멤버 추가 UseCase (PROJECT-004).
 */
public interface AddProjectMemberUseCase {

    /**
     * 프로젝트에 신규 멤버를 추가합니다.
     *
     * @return 생성된 ProjectMember 의 ID
     */
    Long add(AddProjectMemberCommand command);
}
