package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.domain.ProductTeamMembership;
import com.umc.product.organization.domain.enums.ProductTeamRole;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductTeamMembershipJpaRepository extends JpaRepository<ProductTeamMembership, Long> {

    List<ProductTeamMembership> findAllByProductTeamMemberId(Long productTeamMemberId);

    void deleteAllByProductTeamMemberId(Long productTeamMemberId);

    boolean existsByProductTeamMember_MemberIdAndProductTeamGenerationIdAndRoleIn(
        Long memberId,
        Long productTeamGenerationId,
        Set<ProductTeamRole> roles
    );

    boolean existsByProductTeamMember_MemberIdAndProductTeamGenerationIdInAndRoleIn(
        Long memberId,
        Collection<Long> productTeamGenerationIds,
        Set<ProductTeamRole> roles
    );

    @Query("""
        SELECT DISTINCT m.productTeamGenerationId
        FROM ProductTeamMembership m
        WHERE m.productTeamMember.id = :productTeamMemberId
        """)
    List<Long> findDistinctGenerationIdsByProductTeamMemberId(@Param("productTeamMemberId") Long productTeamMemberId);
}
