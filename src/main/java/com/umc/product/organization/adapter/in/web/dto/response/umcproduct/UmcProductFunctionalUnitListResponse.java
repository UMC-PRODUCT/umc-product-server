package com.umc.product.organization.adapter.in.web.dto.response.umcproduct;

import java.util.List;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductFunctionalUnitInfo;

public record UmcProductFunctionalUnitListResponse(
    List<UmcProductFunctionalUnitResponse> functionalUnits
) {
    public static UmcProductFunctionalUnitListResponse from(List<UmcProductFunctionalUnitInfo> infos) {
        return new UmcProductFunctionalUnitListResponse(
            infos.stream().map(UmcProductFunctionalUnitResponse::from).toList()
        );
    }
}
