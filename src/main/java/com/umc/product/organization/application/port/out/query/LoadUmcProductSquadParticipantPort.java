package com.umc.product.organization.application.port.out.query;

import java.util.Collection;
import java.util.List;

import com.umc.product.organization.domain.UmcProductSquadParticipant;

public interface LoadUmcProductSquadParticipantPort {

    List<UmcProductSquadParticipant> listBySquadId(Long squadId);

    List<UmcProductSquadParticipant> listByUmcProductMemberId(Long umcProductMemberId);

    List<UmcProductSquadParticipant> listByUmcProductMemberIds(Collection<Long> umcProductMemberIds);
}
