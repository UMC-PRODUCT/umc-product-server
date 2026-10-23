package com.umc.product.blog.application.port.in.query.dto;

import java.util.List;

public record BlogCommentCursorInfo(
    List<BlogCommentInfo> content,
    Long nextCursor,
    boolean hasNext
) {
}
