package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.application.port.out.command.SaveProductTeamSquadParticipantPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamSquadParticipantPort;
import com.umc.product.organization.domain.ProductTeamSquadParticipant;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTeamSquadParticipantPersistenceAdapter
    implements LoadProductTeamSquadParticipantPort, SaveProductTeamSquadParticipantPort {

    private final ProductTeamSquadParticipantJpaRepository productTeamSquadParticipantJpaRepository;

    @Override
    public List<ProductTeamSquadParticipant> listBySquadId(Long squadId) {
        return productTeamSquadParticipantJpaRepository.findAllBySquadId(squadId);
    }

    @Override
    public List<ProductTeamSquadParticipant> listByProductTeamMemberId(Long productTeamMemberId) {
        return productTeamSquadParticipantJpaRepository.findAllByProductTeamMemberId(productTeamMemberId);
    }

    @Override
    public List<ProductTeamSquadParticipant> listByProductTeamMemberIds(Collection<Long> productTeamMemberIds) {
        if (productTeamMemberIds == null || productTeamMemberIds.isEmpty()) {
            return List.of();
        }
        return productTeamSquadParticipantJpaRepository.findAllByProductTeamMemberIdIn(productTeamMemberIds);
    }

    @Override
    public ProductTeamSquadParticipant save(ProductTeamSquadParticipant participant) {
        return productTeamSquadParticipantJpaRepository.save(participant);
    }

    @Override
    public void saveAll(Collection<ProductTeamSquadParticipant> participants) {
        productTeamSquadParticipantJpaRepository.saveAll(participants);
    }

    @Override
    public void deleteAllBySquadId(Long squadId) {
        productTeamSquadParticipantJpaRepository.deleteAllBySquadId(squadId);
    }

    @Override
    public void deleteAllByProductTeamMemberId(Long productTeamMemberId) {
        productTeamSquadParticipantJpaRepository.deleteAllByProductTeamMemberId(productTeamMemberId);
    }
}
