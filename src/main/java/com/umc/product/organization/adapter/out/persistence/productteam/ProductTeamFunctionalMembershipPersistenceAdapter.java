package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import com.umc.product.organization.application.port.out.command.SaveProductTeamFunctionalMembershipPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalMembershipPort;
import com.umc.product.organization.domain.ProductTeamFunctionalMembership;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTeamFunctionalMembershipPersistenceAdapter
    implements LoadProductTeamFunctionalMembershipPort, SaveProductTeamFunctionalMembershipPort {

    private final ProductTeamFunctionalMembershipJpaRepository productTeamFunctionalMembershipJpaRepository;
    private final ProductTeamMemberQueryRepository productTeamMemberQueryRepository;

    @Override
    public List<ProductTeamFunctionalMembership> listByProductTeamMemberId(Long productTeamMemberId) {
        return productTeamFunctionalMembershipJpaRepository.findAllByProductTeamMemberId(productTeamMemberId);
    }

    @Override
    public List<ProductTeamFunctionalMembership> listByProductTeamMemberIds(
        Collection<Long> productTeamMemberIds,
        ProductTeamMemberSearchCondition condition
    ) {
        return productTeamMemberQueryRepository.listFunctionalMembershipsByMemberIds(productTeamMemberIds, condition);
    }

    @Override
    public List<Long> listGenerationIdsByProductTeamMemberId(Long productTeamMemberId) {
        return productTeamFunctionalMembershipJpaRepository
            .findDistinctGenerationIdsByProductTeamMemberId(productTeamMemberId);
    }

    @Override
    public boolean existsByMemberIdAndGenerationIdAndRoles(
        Long memberId,
        Long productTeamGenerationId,
        Set<ProductTeamFunctionalRole> roles
    ) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return productTeamFunctionalMembershipJpaRepository
            .existsByProductTeamMember_MemberIdAndProductTeamGenerationIdAndRoleIn(
                memberId,
                productTeamGenerationId,
                roles
            );
    }

    @Override
    public boolean existsByMemberIdAndActiveGenerationAndRoles(
        Long memberId,
        Set<ProductTeamFunctionalRole> roles
    ) {
        if (memberId == null || roles == null || roles.isEmpty()) {
            return false;
        }
        return productTeamFunctionalMembershipJpaRepository
            .existsByMemberIdAndActiveGenerationAndRoles(memberId, roles);
    }

    @Override
    public ProductTeamFunctionalMembership save(ProductTeamFunctionalMembership functionalMembership) {
        return productTeamFunctionalMembershipJpaRepository.save(functionalMembership);
    }

    @Override
    public void saveAll(Collection<ProductTeamFunctionalMembership> functionalMemberships) {
        productTeamFunctionalMembershipJpaRepository.saveAll(functionalMemberships);
    }

    @Override
    public void deleteAllByProductTeamMemberId(Long productTeamMemberId) {
        productTeamFunctionalMembershipJpaRepository.deleteAllByProductTeamMemberId(productTeamMemberId);
    }
}
