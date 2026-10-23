package com.umc.product.organization.adapter.in.web.dto.response.gisu;

import java.util.List;

import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기수 목록 응답")
public record GisuListResponse(
    @Schema(description = "기수 목록")
    List<GisuResponse> gisuList
) {
    public static GisuListResponse from(List<GisuInfo> gisuInfoList) {
        List<GisuResponse> responses = gisuInfoList.stream().map(GisuResponse::from).toList();
        return new GisuListResponse(responses);
    }
}
