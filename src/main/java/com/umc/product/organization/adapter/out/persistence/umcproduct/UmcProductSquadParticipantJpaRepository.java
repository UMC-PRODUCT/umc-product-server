package com.umc.product.organization.adapter.out.persistence.umcproduct;

import com.umc.product.organization.domain.UmcProductSquadParticipant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UmcProductSquadParticipantJpaRepository extends JpaRepository<UmcProductSquadParticipant, Long> {

    List<UmcProductSquadParticipant> findAllBySquadId(Long squadId);

    List<UmcProductSquadParticipant> findAllByUmcProductMemberId(Long umcProductMemberId);

    List<UmcProductSquadParticipant> findAllByUmcProductMemberIdIn(Collection<Long> umcProductMemberIds);

    void deleteAllBySquadId(Long squadId);

    void deleteAllByUmcProductMemberId(Long umcProductMemberId);
}
