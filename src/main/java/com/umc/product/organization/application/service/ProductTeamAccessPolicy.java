package com.umc.product.organization.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalMembershipPort;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTeamAccessPolicy {

    public static final Set<ProductTeamFunctionalRole> MANAGER_ROLES = Set.of(
        ProductTeamFunctionalRole.PRODUCT_LEAD,
        ProductTeamFunctionalRole.PRODUCT_VICE_LEAD
    );

    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final LoadProductTeamFunctionalMembershipPort loadProductTeamFunctionalMembershipPort;

    public boolean canCreateGeneration(Long requesterMemberId) {
        return isCentralCore(requesterMemberId);
    }

    public boolean canManageGeneration(Long requesterMemberId, Long productTeamGenerationId) {
        if (isCentralCore(requesterMemberId)) {
            return true;
        }
        if (requesterMemberId == null || productTeamGenerationId == null) {
            return false;
        }
        return loadProductTeamFunctionalMembershipPort.existsByMemberIdAndGenerationIdAndRoles(
            requesterMemberId,
            productTeamGenerationId,
            MANAGER_ROLES
        );
    }

    public boolean canManageProductTeam(Long requesterMemberId) {
        if (isCentralCore(requesterMemberId)) {
            return true;
        }
        if (requesterMemberId == null) {
            return false;
        }
        return loadProductTeamFunctionalMembershipPort.existsByMemberIdAndActiveGenerationAndRoles(
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
        return canManageProductTeam(requesterMemberId);
    }

    @SuppressWarnings("removal")
    private boolean isCentralCore(Long requesterMemberId) {
        return requesterMemberId != null && getChallengerRoleUseCase.isCentralCore(requesterMemberId);
    }
}
