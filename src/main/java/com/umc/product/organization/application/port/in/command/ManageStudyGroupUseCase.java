package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateStudyGroupCommand;
import com.umc.product.organization.application.port.in.command.dto.AddStudyGroupMembersCommand;

public interface ManageStudyGroupUseCase {

    void create(CreateStudyGroupCommand command);

    void update(UpdateStudyGroupCommand command);

    void addMembers(AddStudyGroupMembersCommand command);

    void delete(Long groupId);
}
