package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamGenerationInfo;
import java.util.List;

public record ProductTeamGenerationListResponse(
    List<ProductTeamGenerationResponse> generations
) {
    public static ProductTeamGenerationListResponse from(List<ProductTeamGenerationInfo> infos) {
        return new ProductTeamGenerationListResponse(
            infos.stream().map(ProductTeamGenerationResponse::from).toList()
        );
    }
}
