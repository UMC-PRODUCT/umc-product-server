package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamSquadInfo;
import java.util.List;

public interface GetProductTeamSquadUseCase {

    List<ProductTeamSquadInfo> list(Long productTeamGenerationId, Boolean active);
}
