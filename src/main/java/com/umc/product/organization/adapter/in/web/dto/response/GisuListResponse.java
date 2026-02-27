package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

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
