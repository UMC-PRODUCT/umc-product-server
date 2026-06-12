package com.umc.product.organization.adapter.in.web.dto.response.umcproduct;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductGenerationInfo;
import java.util.List;

public record UmcProductGenerationListResponse(
    List<UmcProductGenerationResponse> generations
) {
    public static UmcProductGenerationListResponse from(List<UmcProductGenerationInfo> infos) {
        return new UmcProductGenerationListResponse(
            infos.stream().map(UmcProductGenerationResponse::from).toList()
        );
    }
}
