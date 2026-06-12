package com.umc.product.organization.adapter.out.persistence.umcproduct;

import com.umc.product.organization.domain.UmcProductFunctionalMembership;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UmcProductFunctionalMembershipJpaRepository
    extends JpaRepository<UmcProductFunctionalMembership, Long> {

    List<UmcProductFunctionalMembership> findAllByUmcProductMemberId(Long umcProductMemberId);

    void deleteAllByUmcProductMemberId(Long umcProductMemberId);

    boolean existsByUmcProductMember_MemberIdAndUmcProductGenerationIdAndRoleIn(
        Long memberId,
        Long umcProductGenerationId,
        Set<UmcProductFunctionalRole> roles
    );

    @Query("""
        SELECT COUNT(m) > 0
        FROM UmcProductFunctionalMembership m
        JOIN UmcProductGeneration g ON g.id = m.umcProductGenerationId
        WHERE m.umcProductMember.memberId = :memberId
          AND g.isActive = true
          AND m.role IN :roles
        """)
    boolean existsByMemberIdAndActiveGenerationAndRoles(
        @Param("memberId") Long memberId,
        @Param("roles") Set<UmcProductFunctionalRole> roles
    );

    @Query("""
        SELECT DISTINCT m.umcProductGenerationId
        FROM UmcProductFunctionalMembership m
        WHERE m.umcProductMember.id = :umcProductMemberId
        """)
    List<Long> findDistinctGenerationIdsByUmcProductMemberId(@Param("umcProductMemberId") Long umcProductMemberId);
}
