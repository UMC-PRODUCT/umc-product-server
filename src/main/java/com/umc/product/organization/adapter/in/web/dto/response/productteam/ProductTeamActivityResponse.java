package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamActivityInfo;
import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;

public record ProductTeamActivityResponse(
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
    public static ProductTeamActivityResponse from(ProductTeamActivityInfo info) {
        return new ProductTeamActivityResponse(
            info.productTeamMembershipId(),
            info.productTeamGenerationId(),
            info.generation(),
            info.part(),
            info.partName(),
            info.role(),
            info.roleName(),
            info.position(),
            info.positionName()
        );
    }
}
