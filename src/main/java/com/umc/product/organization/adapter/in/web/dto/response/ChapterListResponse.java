package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import java.util.List;

public record ChapterListResponse(
        List<ChapterItem> chapters
) {
    public static ChapterListResponse from(List<ChapterInfo> infos) {
        List<ChapterItem> items = infos.stream()
                .map(ChapterItem::from)
                .toList();
        return new ChapterListResponse(items);
    }

    public record ChapterItem(
            Long id,
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
