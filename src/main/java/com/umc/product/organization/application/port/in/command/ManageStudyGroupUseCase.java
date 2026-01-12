package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupComand;

public interface ManageStudyGroupUseCase {

    void registerStudyGroup(CreateStudyGroupComand command);
}
