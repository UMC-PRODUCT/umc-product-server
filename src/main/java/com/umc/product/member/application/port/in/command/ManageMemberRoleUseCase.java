package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.UpdateMemberRoleCommand;

public interface ManageMemberRoleUseCase {

    void updateRole(UpdateMemberRoleCommand command);
}
