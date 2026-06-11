package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamSquadCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceProductTeamSquadParticipantsCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamSquadCommand;

public interface ManageProductTeamSquadUseCase {

    Long create(CreateProductTeamSquadCommand command);

    void update(UpdateProductTeamSquadCommand command);

    void delete(Long squadId, Long requesterMemberId);

    void replaceParticipants(ReplaceProductTeamSquadParticipantsCommand command);
}
