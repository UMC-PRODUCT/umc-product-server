package com.umc.product.blog.application.port.in.query.dto;

import java.util.List;

public record BlogHashtagCursorInfo(
    List<BlogHashtagInfo> content,
    Long nextCursor,
    boolean hasNext
) {
}
