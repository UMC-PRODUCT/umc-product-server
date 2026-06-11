package com.umc.product.organization.application.port.in.query.dto.productteam;

import java.util.List;

public record ProductTeamOrganizationChartInfo(
    ProductTeamGenerationInfo generation,
    List<ProductTeamFunctionalUnitInfo> functionalUnits,
    List<ProductTeamSquadInfo> squads
) {
}
