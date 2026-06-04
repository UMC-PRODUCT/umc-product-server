package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import com.umc.product.organization.application.port.out.command.SaveProductTeamMembershipPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMembershipPort;
import com.umc.product.organization.domain.ProductTeamMembership;
import com.umc.product.organization.domain.enums.ProductTeamRole;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTeamMembershipPersistenceAdapter
    implements LoadProductTeamMembershipPort, SaveProductTeamMembershipPort {

    private final ProductTeamMembershipJpaRepository productTeamMembershipJpaRepository;
    private final ProductTeamMemberQueryRepository productTeamMemberQueryRepository;

    @Override
    public List<ProductTeamMembership> listByProductTeamMemberId(Long productTeamMemberId) {
        return productTeamMembershipJpaRepository.findAllByProductTeamMemberId(productTeamMemberId);
    }

    @Override
    public List<ProductTeamMembership> listByProductTeamMemberIds(
        Collection<Long> productTeamMemberIds,
        ProductTeamMemberSearchCondition condition
    ) {
        return productTeamMemberQueryRepository.listMembershipsByMemberIds(productTeamMemberIds, condition);
    }

    @Override
    public List<Long> listGenerationIdsByProductTeamMemberId(Long productTeamMemberId) {
        return productTeamMembershipJpaRepository.findDistinctGenerationIdsByProductTeamMemberId(productTeamMemberId);
    }

    @Override
    public boolean existsByMemberIdAndGenerationIdAndRoles(
        Long memberId,
        Long productTeamGenerationId,
        Set<ProductTeamRole> roles
    ) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return productTeamMembershipJpaRepository.existsByProductTeamMember_MemberIdAndProductTeamGenerationIdAndRoleIn(
            memberId,
            productTeamGenerationId,
            roles
        );
    }

    @Override
    public boolean existsByMemberIdAndGenerationIdInAndRoles(
        Long memberId,
        Collection<Long> productTeamGenerationIds,
        Set<ProductTeamRole> roles
    ) {
        if (productTeamGenerationIds == null || productTeamGenerationIds.isEmpty()
            || roles == null || roles.isEmpty()) {
            return false;
        }
        return productTeamMembershipJpaRepository
            .existsByProductTeamMember_MemberIdAndProductTeamGenerationIdInAndRoleIn(
                memberId,
                productTeamGenerationIds,
                roles
            );
    }

    @Override
    public ProductTeamMembership save(ProductTeamMembership membership) {
        return productTeamMembershipJpaRepository.save(membership);
    }

    @Override
    public void saveAll(Collection<ProductTeamMembership> memberships) {
        productTeamMembershipJpaRepository.saveAll(memberships);
    }

    @Override
    public void deleteAllByProductTeamMemberId(Long productTeamMemberId) {
        productTeamMembershipJpaRepository.deleteAllByProductTeamMemberId(productTeamMemberId);
    }
}
