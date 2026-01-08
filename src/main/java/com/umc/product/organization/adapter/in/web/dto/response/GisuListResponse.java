package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.util.List;

public record GisuListResponse(List<GisuResponse> gisuList) {
    public static GisuListResponse from(List<GisuInfo> gisuInfoList) {
        List<GisuResponse> responses = gisuInfoList.stream().map(GisuResponse::from).toList();
        return new GisuListResponse(responses);
    }
}
