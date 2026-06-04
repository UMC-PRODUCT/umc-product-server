package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamGenerationInfo;
import java.util.List;

public interface GetProductTeamGenerationUseCase {

    ProductTeamGenerationInfo getById(Long productTeamGenerationId);

    List<ProductTeamGenerationInfo> listAll();
}
