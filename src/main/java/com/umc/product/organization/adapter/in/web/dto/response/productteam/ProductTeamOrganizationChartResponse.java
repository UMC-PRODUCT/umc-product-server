package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamOrganizationChartInfo;
import java.util.List;

public record ProductTeamOrganizationChartResponse(
    ProductTeamGenerationResponse generation,
    List<ProductTeamFunctionalUnitResponse> functionalUnits,
    List<ProductTeamSquadResponse> squads
) {
    public static ProductTeamOrganizationChartResponse from(ProductTeamOrganizationChartInfo info) {
        return new ProductTeamOrganizationChartResponse(
            ProductTeamGenerationResponse.from(info.generation()),
            info.functionalUnits().stream().map(ProductTeamFunctionalUnitResponse::from).toList(),
            info.squads().stream().map(ProductTeamSquadResponse::from).toList()
        );
    }
}
