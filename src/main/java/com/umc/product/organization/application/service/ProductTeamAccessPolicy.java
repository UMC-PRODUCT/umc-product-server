package com.umc.product.organization.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMembershipPort;
import com.umc.product.organization.domain.enums.ProductTeamRole;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTeamAccessPolicy {

    public static final Set<ProductTeamRole> MANAGER_ROLES = Set.of(
        ProductTeamRole.PRODUCT_LEAD,
        ProductTeamRole.PRODUCT_VICE_LEAD,
        ProductTeamRole.GENERAL_MANAGER
    );

    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final LoadProductTeamMembershipPort loadProductTeamMembershipPort;

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
        return loadProductTeamMembershipPort.existsByMemberIdAndGenerationIdAndRoles(
            requesterMemberId,
            productTeamGenerationId,
            MANAGER_ROLES
        );
    }

    public boolean canManageAllGenerations(Long requesterMemberId, Collection<Long> productTeamGenerationIds) {
        if (isCentralCore(requesterMemberId)) {
            return true;
        }
        if (requesterMemberId == null || productTeamGenerationIds == null || productTeamGenerationIds.isEmpty()) {
            return false;
        }
        List<Long> generationIds = productTeamGenerationIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (generationIds.isEmpty()) {
            return false;
        }
        return generationIds.stream()
            .allMatch(generationId -> canManageGeneration(requesterMemberId, generationId));
    }

    public boolean canManageMemberProfile(
        Long requesterMemberId,
        Long targetMemberId,
        Collection<Long> targetGenerationIds
    ) {
        if (Objects.equals(requesterMemberId, targetMemberId)) {
            return true;
        }
        if (isCentralCore(requesterMemberId)) {
            return true;
        }
        return loadProductTeamMembershipPort.existsByMemberIdAndGenerationIdInAndRoles(
            requesterMemberId,
            targetGenerationIds,
            MANAGER_ROLES
        );
    }

    @SuppressWarnings("removal")
    private boolean isCentralCore(Long requesterMemberId) {
        return requesterMemberId != null && getChallengerRoleUseCase.isCentralCore(requesterMemberId);
    }
}
