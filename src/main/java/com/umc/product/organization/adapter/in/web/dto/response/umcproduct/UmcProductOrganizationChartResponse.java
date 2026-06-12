package com.umc.product.organization.adapter.in.web.dto.response.umcproduct;

import java.util.List;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductOrganizationChartInfo;

public record UmcProductOrganizationChartResponse(
    UmcProductGenerationResponse generation,
    List<UmcProductFunctionalUnitResponse> functionalUnits,
    List<UmcProductSquadResponse> squads
) {
    public static UmcProductOrganizationChartResponse from(UmcProductOrganizationChartInfo info) {
        return new UmcProductOrganizationChartResponse(
            UmcProductGenerationResponse.from(info.generation()),
            info.functionalUnits().stream().map(UmcProductFunctionalUnitResponse::from).toList(),
            info.squads().stream().map(UmcProductSquadResponse::from).toList()
        );
    }
}
