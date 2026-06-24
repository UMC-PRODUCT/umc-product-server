package com.umc.product.blog.application.port.in.query.dto;

import java.util.List;

public record BlogSeriesCursorInfo(
    List<BlogSeriesSummaryInfo> content,
    Long nextCursor,
    boolean hasNext
) {
}
