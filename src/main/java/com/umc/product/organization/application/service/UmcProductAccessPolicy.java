package com.umc.product.organization.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalMembershipPort;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UmcProductAccessPolicy {

    public static final Set<UmcProductFunctionalRole> MANAGER_ROLES = Set.of(
        UmcProductFunctionalRole.UMC_PRODUCT_LEAD,
        UmcProductFunctionalRole.UMC_PRODUCT_VICE_LEAD
    );

    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final LoadUmcProductFunctionalMembershipPort loadUmcProductFunctionalMembershipPort;

    public boolean canCreateGeneration(Long requesterMemberId) {
        return isCentralCore(requesterMemberId);
    }

    public boolean canManageGeneration(Long requesterMemberId, Long umcProductGenerationId) {
        if (isCentralCore(requesterMemberId)) {
            return true;
        }
        if (requesterMemberId == null || umcProductGenerationId == null) {
            return false;
        }
        return loadUmcProductFunctionalMembershipPort.existsByMemberIdAndGenerationIdAndRoles(
            requesterMemberId,
            umcProductGenerationId,
            MANAGER_ROLES
        );
    }

    public boolean canManageUmcProduct(Long requesterMemberId) {
        if (isCentralCore(requesterMemberId)) {
            return true;
        }
        if (requesterMemberId == null) {
            return false;
        }
        return loadUmcProductFunctionalMembershipPort.existsByMemberIdAndActiveGenerationAndRoles(
            requesterMemberId,
            MANAGER_ROLES
        );
    }

    public boolean canManageMemberProfile(
        Long requesterMemberId,
        Long targetMemberId
    ) {
        if (requesterMemberId != null && requesterMemberId.equals(targetMemberId)) {
            return true;
        }
        return canManageUmcProduct(requesterMemberId);
    }

    @SuppressWarnings("removal")
    private boolean isCentralCore(Long requesterMemberId) {
        return requesterMemberId != null && getChallengerRoleUseCase.isCentralCore(requesterMemberId);
    }
}
