package com.umc.product.blog.application.port.in.command.dto;

import java.util.List;

public record ReplaceBlogSeriesContentsCommand(
    Long seriesId,
    List<Long> contentIds
) {
    public static ReplaceBlogSeriesContentsCommand of(Long seriesId, List<Long> contentIds) {
        return new ReplaceBlogSeriesContentsCommand(seriesId, contentIds == null ? List.of() : List.copyOf(contentIds));
    }
}
