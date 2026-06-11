package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.domain.ProductTeamSquadParticipant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTeamSquadParticipantJpaRepository extends JpaRepository<ProductTeamSquadParticipant, Long> {

    List<ProductTeamSquadParticipant> findAllBySquadId(Long squadId);

    List<ProductTeamSquadParticipant> findAllByProductTeamMemberId(Long productTeamMemberId);

    List<ProductTeamSquadParticipant> findAllByProductTeamMemberIdIn(Collection<Long> productTeamMemberIds);

    void deleteAllBySquadId(Long squadId);

    void deleteAllByProductTeamMemberId(Long productTeamMemberId);
}
