package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceProductTeamMemberFunctionalMembershipsCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamMemberProfileCommand;

public interface ManageProductTeamMemberUseCase {

    Long create(CreateProductTeamMemberCommand command);

    void updateProfile(UpdateProductTeamMemberProfileCommand command);

    void replaceFunctionalMemberships(ReplaceProductTeamMemberFunctionalMembershipsCommand command);

    void delete(Long productTeamMemberId, Long requesterMemberId);
}
