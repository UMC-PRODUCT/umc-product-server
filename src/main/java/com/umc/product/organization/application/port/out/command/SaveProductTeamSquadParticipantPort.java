package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.ProductTeamSquadParticipant;
import java.util.Collection;

public interface SaveProductTeamSquadParticipantPort {

    ProductTeamSquadParticipant save(ProductTeamSquadParticipant participant);

    void saveAll(Collection<ProductTeamSquadParticipant> participants);

    void deleteAllBySquadId(Long squadId);

    void deleteAllByProductTeamMemberId(Long productTeamMemberId);
}
