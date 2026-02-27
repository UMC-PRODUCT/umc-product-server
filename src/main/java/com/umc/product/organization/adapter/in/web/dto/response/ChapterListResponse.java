package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "지부 목록 응답")
public record ChapterListResponse(
        @Schema(description = "지부 목록")
        List<ChapterItem> chapters
) {
    public static ChapterListResponse from(List<ChapterInfo> infos) {
        List<ChapterItem> items = infos.stream()
                .map(ChapterItem::from)
                .toList();
        return new ChapterListResponse(items);
    }

    @Schema(description = "지부 정보")
    public record ChapterItem(
            @Schema(description = "지부 ID", example = "1")
            Long id,
            @Schema(description = "지부명", example = "서울")
            String name
    ) {
        public static ChapterItem from(ChapterInfo info) {
            return new ChapterItem(
                    info.id(),
                    info.name()
            );
        }
    }
}
