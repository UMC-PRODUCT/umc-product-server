package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;
import java.util.Objects;

public record ProductTeamActivityCommand(
    Long productTeamGenerationId,
    ProductTeamPart part,
    ProductTeamRole role,
    ProductTeamPosition position
) {
    public ProductTeamActivityCommand {
        Objects.requireNonNull(productTeamGenerationId, "productTeamGenerationId must not be null");
        Objects.requireNonNull(part, "part must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(position, "position must not be null");
    }

    public static ProductTeamActivityCommand of(
        Long productTeamGenerationId,
        ProductTeamPart part,
        ProductTeamRole role,
        ProductTeamPosition position
    ) {
        return new ProductTeamActivityCommand(productTeamGenerationId, part, role, position);
    }
}
