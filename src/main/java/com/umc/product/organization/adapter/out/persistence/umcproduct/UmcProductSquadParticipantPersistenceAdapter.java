package com.umc.product.organization.adapter.out.persistence.umcproduct;

import com.umc.product.organization.application.port.out.command.SaveUmcProductSquadParticipantPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadParticipantPort;
import com.umc.product.organization.domain.UmcProductSquadParticipant;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UmcProductSquadParticipantPersistenceAdapter
    implements LoadUmcProductSquadParticipantPort, SaveUmcProductSquadParticipantPort {

    private final UmcProductSquadParticipantJpaRepository umcProductSquadParticipantJpaRepository;

    @Override
    public List<UmcProductSquadParticipant> listBySquadId(Long squadId) {
        return umcProductSquadParticipantJpaRepository.findAllBySquadId(squadId);
    }

    @Override
    public List<UmcProductSquadParticipant> listByUmcProductMemberId(Long umcProductMemberId) {
        return umcProductSquadParticipantJpaRepository.findAllByUmcProductMemberId(umcProductMemberId);
    }

    @Override
    public List<UmcProductSquadParticipant> listByUmcProductMemberIds(Collection<Long> umcProductMemberIds) {
        if (umcProductMemberIds == null || umcProductMemberIds.isEmpty()) {
            return List.of();
        }
        return umcProductSquadParticipantJpaRepository.findAllByUmcProductMemberIdIn(umcProductMemberIds);
    }

    @Override
    public UmcProductSquadParticipant save(UmcProductSquadParticipant participant) {
        return umcProductSquadParticipantJpaRepository.save(participant);
    }

    @Override
    public void saveAll(Collection<UmcProductSquadParticipant> participants) {
        umcProductSquadParticipantJpaRepository.saveAll(participants);
    }

    @Override
    public void deleteAllBySquadId(Long squadId) {
        umcProductSquadParticipantJpaRepository.deleteAllBySquadId(squadId);
    }

    @Override
    public void deleteAllByUmcProductMemberId(Long umcProductMemberId) {
        umcProductSquadParticipantJpaRepository.deleteAllByUmcProductMemberId(umcProductMemberId);
    }
}
