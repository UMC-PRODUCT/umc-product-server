package com.umc.product.organization.application.port.in.query.dto.umcproduct;

import java.util.List;

public record UmcProductOrganizationChartInfo(
    UmcProductGenerationInfo generation,
    List<UmcProductFunctionalUnitInfo> functionalUnits,
    List<UmcProductSquadInfo> squads
) {
}
