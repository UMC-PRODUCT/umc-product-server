package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.domain.ProductTeamFunctionalMembership;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductTeamFunctionalMembershipJpaRepository
    extends JpaRepository<ProductTeamFunctionalMembership, Long> {

    List<ProductTeamFunctionalMembership> findAllByProductTeamMemberId(Long productTeamMemberId);

    void deleteAllByProductTeamMemberId(Long productTeamMemberId);

    boolean existsByProductTeamMember_MemberIdAndProductTeamGenerationIdAndRoleIn(
        Long memberId,
        Long productTeamGenerationId,
        Set<ProductTeamFunctionalRole> roles
    );

    @Query("""
        SELECT COUNT(m) > 0
        FROM ProductTeamFunctionalMembership m
        JOIN ProductTeamGeneration g ON g.id = m.productTeamGenerationId
        WHERE m.productTeamMember.memberId = :memberId
          AND g.isActive = true
          AND m.role IN :roles
        """)
    boolean existsByMemberIdAndActiveGenerationAndRoles(
        @Param("memberId") Long memberId,
        @Param("roles") Set<ProductTeamFunctionalRole> roles
    );

    @Query("""
        SELECT DISTINCT m.productTeamGenerationId
        FROM ProductTeamFunctionalMembership m
        WHERE m.productTeamMember.id = :productTeamMemberId
        """)
    List<Long> findDistinctGenerationIdsByProductTeamMemberId(@Param("productTeamMemberId") Long productTeamMemberId);
}
