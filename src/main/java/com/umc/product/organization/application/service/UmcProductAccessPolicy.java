package com.umc.product.organization.application.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalMembershipPort;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;

import lombok.RequiredArgsConstructor;

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
        return isCentralCoreInAnyGisu(requesterMemberId);
    }

    public boolean canManageGeneration(Long requesterMemberId, Long umcProductGenerationId) {
        if (isCentralCoreInAnyGisu(requesterMemberId)) {
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
        if (isCentralCoreInAnyGisu(requesterMemberId)) {
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

    private boolean isCentralCoreInAnyGisu(Long requesterMemberId) {
        return requesterMemberId != null && getChallengerRoleUseCase.isCentralCoreInAnyGisu(requesterMemberId);
    }
}
