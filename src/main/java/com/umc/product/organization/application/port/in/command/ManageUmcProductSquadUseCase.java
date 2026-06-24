package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductSquadCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceUmcProductSquadParticipantsCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductSquadCommand;

public interface ManageUmcProductSquadUseCase {

    Long create(CreateUmcProductSquadCommand command);

    void update(UpdateUmcProductSquadCommand command);

    void delete(Long squadId, Long requesterMemberId);

    void replaceParticipants(ReplaceUmcProductSquadParticipantsCommand command);
}
