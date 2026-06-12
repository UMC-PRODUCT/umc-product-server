package com.umc.product.organization.application.port.out.command;

import java.util.Collection;

import com.umc.product.organization.domain.UmcProductSquadParticipant;

public interface SaveUmcProductSquadParticipantPort {

    UmcProductSquadParticipant save(UmcProductSquadParticipant participant);

    void saveAll(Collection<UmcProductSquadParticipant> participants);

    void deleteAllBySquadId(Long squadId);

    void deleteAllByUmcProductMemberId(Long umcProductMemberId);
}
