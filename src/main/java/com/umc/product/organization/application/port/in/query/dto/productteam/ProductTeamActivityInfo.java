package com.umc.product.organization.application.port.in.query.dto.productteam;

import com.umc.product.organization.domain.ProductTeamMembership;
import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;

public record ProductTeamActivityInfo(
    Long productTeamMembershipId,
    Long productTeamGenerationId,
    Long generation,
    ProductTeamPart part,
    String partName,
    ProductTeamRole role,
    String roleName,
    ProductTeamPosition position,
    String positionName
) {
    public static ProductTeamActivityInfo from(ProductTeamMembership membership, ProductTeamGenerationInfo generation) {
        Long generationNumber = generation == null ? null : generation.generation();
        return new ProductTeamActivityInfo(
            membership.getId(),
            membership.getProductTeamGenerationId(),
            generationNumber,
            membership.getPart(),
            membership.getPart().getDisplayName(),
            membership.getRole(),
            membership.getRole().getDisplayName(),
            membership.getPosition(),
            membership.getPosition().getDisplayName()
        );
    }
}
