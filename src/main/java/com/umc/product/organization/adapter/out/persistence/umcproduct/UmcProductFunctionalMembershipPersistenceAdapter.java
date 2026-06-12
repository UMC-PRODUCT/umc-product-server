package com.umc.product.organization.adapter.out.persistence.umcproduct;

import com.umc.product.organization.application.port.out.command.SaveUmcProductFunctionalMembershipPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalMembershipPort;
import com.umc.product.organization.domain.UmcProductFunctionalMembership;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UmcProductFunctionalMembershipPersistenceAdapter
    implements LoadUmcProductFunctionalMembershipPort, SaveUmcProductFunctionalMembershipPort {

    private final UmcProductFunctionalMembershipJpaRepository umcProductFunctionalMembershipJpaRepository;
    private final UmcProductMemberQueryRepository umcProductMemberQueryRepository;

    @Override
    public List<UmcProductFunctionalMembership> listByUmcProductMemberId(Long umcProductMemberId) {
        return umcProductFunctionalMembershipJpaRepository.findAllByUmcProductMemberId(umcProductMemberId);
    }

    @Override
    public List<UmcProductFunctionalMembership> listByUmcProductMemberIds(Collection<Long> umcProductMemberIds) {
        return umcProductMemberQueryRepository.listFunctionalMembershipsByMemberIds(umcProductMemberIds);
    }

    @Override
    public List<Long> listGenerationIdsByUmcProductMemberId(Long umcProductMemberId) {
        return umcProductFunctionalMembershipJpaRepository
            .findDistinctGenerationIdsByUmcProductMemberId(umcProductMemberId);
    }

    @Override
    public boolean existsByMemberIdAndGenerationIdAndRoles(
        Long memberId,
        Long umcProductGenerationId,
        Set<UmcProductFunctionalRole> roles
    ) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return umcProductFunctionalMembershipJpaRepository
            .existsByUmcProductMember_MemberIdAndUmcProductGenerationIdAndRoleIn(
                memberId,
                umcProductGenerationId,
                roles
            );
    }

    @Override
    public boolean existsByMemberIdAndActiveGenerationAndRoles(
        Long memberId,
        Set<UmcProductFunctionalRole> roles
    ) {
        if (memberId == null || roles == null || roles.isEmpty()) {
            return false;
        }
        return umcProductFunctionalMembershipJpaRepository
            .existsByMemberIdAndActiveGenerationAndRoles(memberId, roles);
    }

    @Override
    public UmcProductFunctionalMembership save(UmcProductFunctionalMembership functionalMembership) {
        return umcProductFunctionalMembershipJpaRepository.save(functionalMembership);
    }

    @Override
    public void saveAll(Collection<UmcProductFunctionalMembership> functionalMemberships) {
        umcProductFunctionalMembershipJpaRepository.saveAll(functionalMemberships);
    }

    @Override
    public void deleteAllByUmcProductMemberId(Long umcProductMemberId) {
        umcProductFunctionalMembershipJpaRepository.deleteAllByUmcProductMemberId(umcProductMemberId);
    }
}
