package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.GisuNameInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "기수 전체 목록 응답")
public record GisuNameListResponse(
        @Schema(description = "기수 목록")
        List<GisuNameItem> gisuList
) {
    public static GisuNameListResponse from(List<GisuNameInfo> infos) {
        List<GisuNameItem> gisuList = infos.stream()
                .map(GisuNameItem::from)
                .toList();
        return new GisuNameListResponse(gisuList);
    }

    @Schema(description = "기수 정보")
    public record GisuNameItem(
            @Schema(description = "기수 ID", example = "1")
            Long gisuId,
            @Schema(description = "기수 번호", example = "8")
            Long generation,
            @Schema(description = "활성 여부", example = "true")
            boolean isActive
    ) {
        public static GisuNameItem from(GisuNameInfo info) {
            return new GisuNameItem(info.gisuId(), info.generation(), info.isActive());
        }
    }
}
