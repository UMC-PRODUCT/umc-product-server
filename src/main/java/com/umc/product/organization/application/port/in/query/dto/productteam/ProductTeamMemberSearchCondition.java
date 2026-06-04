package com.umc.product.organization.application.port.in.query.dto.productteam;

import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;

public record ProductTeamMemberSearchCondition(
    Long productTeamGenerationId,
    ProductTeamPart part,
    ProductTeamRole role,
    ProductTeamPosition position
) {
    public static ProductTeamMemberSearchCondition of(
        Long productTeamGenerationId,
        ProductTeamPart part,
        ProductTeamRole role,
        ProductTeamPosition position
    ) {
        return new ProductTeamMemberSearchCondition(productTeamGenerationId, part, role, position);
    }
}
