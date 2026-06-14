package com.umc.product.organization.adapter.out.persistence.umcproduct;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.organization.domain.UmcProductSquadParticipant;

public interface UmcProductSquadParticipantJpaRepository extends JpaRepository<UmcProductSquadParticipant, Long> {

    List<UmcProductSquadParticipant> findAllBySquadId(Long squadId);

    List<UmcProductSquadParticipant> findAllByUmcProductMemberId(Long umcProductMemberId);

    List<UmcProductSquadParticipant> findAllByUmcProductMemberIdIn(Collection<Long> umcProductMemberIds);

    @Modifying
    @Query("""
        DELETE FROM UmcProductSquadParticipant p
        WHERE p.squad.id = :squadId
        """)
    void deleteAllBySquadId(@Param("squadId") Long squadId);

    @Modifying
    @Query("""
        DELETE FROM UmcProductSquadParticipant p
        WHERE p.umcProductMember.id = :umcProductMemberId
        """)
    void deleteAllByUmcProductMemberId(@Param("umcProductMemberId") Long umcProductMemberId);
}
