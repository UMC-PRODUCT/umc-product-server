package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamOrganizationChartInfo;

public interface GetProductTeamOrganizationChartUseCase {

    ProductTeamOrganizationChartInfo getByGenerationId(Long productTeamGenerationId);
}
