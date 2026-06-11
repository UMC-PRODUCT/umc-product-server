package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamFunctionalUnitInfo;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import java.util.List;

public interface GetProductTeamFunctionalUnitUseCase {

    List<ProductTeamFunctionalUnitInfo> listByGeneration(Long productTeamGenerationId, ProductTeamFunctionalUnitType type);
}
