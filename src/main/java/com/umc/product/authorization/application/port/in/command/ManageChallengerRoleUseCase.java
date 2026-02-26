package com.umc.product.authorization.application.port.in.command;

import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.command.dto.DeleteChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.command.dto.UpdateChallengerRoleCommand;
import java.util.List;

public interface ManageChallengerRoleUseCase {

    /**
     * 챌린저에게 역할을 부여합니다.
     */
    Long createChallengerRole(CreateChallengerRoleCommand command);

    /**
     * 여러 챌린저에게 역할을 일괄 부여합니다.
     */
    List<Long> createChallengerRoleBulk(List<CreateChallengerRoleCommand> commands);

    /**
     * 챌린저 역할을 수정합니다.
     */
    void updateChallengerRole(UpdateChallengerRoleCommand command);

    /**
     * 챌린저 역할을 삭제합니다.
     */
    void deleteChallengerRole(DeleteChallengerRoleCommand command);
}