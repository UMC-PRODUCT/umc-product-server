package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamFunctionalUnitInfo;
import java.util.List;

public record ProductTeamFunctionalUnitListResponse(
    List<ProductTeamFunctionalUnitResponse> functionalUnits
) {
    public static ProductTeamFunctionalUnitListResponse from(List<ProductTeamFunctionalUnitInfo> infos) {
        return new ProductTeamFunctionalUnitListResponse(
            infos.stream().map(ProductTeamFunctionalUnitResponse::from).toList()
        );
    }
}
