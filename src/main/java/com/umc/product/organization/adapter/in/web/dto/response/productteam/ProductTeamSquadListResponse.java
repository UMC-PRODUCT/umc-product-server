package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamSquadInfo;
import java.util.List;

public record ProductTeamSquadListResponse(
    List<ProductTeamSquadResponse> squads
) {
    public static ProductTeamSquadListResponse from(List<ProductTeamSquadInfo> infos) {
        return new ProductTeamSquadListResponse(
            infos.stream().map(ProductTeamSquadResponse::from).toList()
        );
    }
}
