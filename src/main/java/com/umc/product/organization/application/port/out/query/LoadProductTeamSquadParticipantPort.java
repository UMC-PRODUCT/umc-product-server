package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.ProductTeamSquadParticipant;
import java.util.Collection;
import java.util.List;

public interface LoadProductTeamSquadParticipantPort {

    List<ProductTeamSquadParticipant> listBySquadId(Long squadId);

    List<ProductTeamSquadParticipant> listByProductTeamMemberId(Long productTeamMemberId);

    List<ProductTeamSquadParticipant> listByProductTeamMemberIds(Collection<Long> productTeamMemberIds);
}
