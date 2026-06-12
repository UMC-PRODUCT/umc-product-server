package com.umc.product.organization.adapter.in.web.dto.response.umcproduct;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductSquadInfo;
import java.util.List;

public record UmcProductSquadListResponse(
    List<UmcProductSquadResponse> squads
) {
    public static UmcProductSquadListResponse from(List<UmcProductSquadInfo> infos) {
        return new UmcProductSquadListResponse(
            infos.stream().map(UmcProductSquadResponse::from).toList()
        );
    }
}
