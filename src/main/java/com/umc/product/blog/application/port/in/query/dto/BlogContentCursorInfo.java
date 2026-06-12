package com.umc.product.blog.application.port.in.query.dto;

import java.util.List;

public record BlogContentCursorInfo(
    List<BlogContentSummaryInfo> content,
    Long nextCursor,
    boolean hasNext
) {
}
