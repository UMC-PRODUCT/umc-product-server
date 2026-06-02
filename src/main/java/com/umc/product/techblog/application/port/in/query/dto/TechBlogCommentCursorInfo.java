package com.umc.product.techblog.application.port.in.query.dto;

import java.util.List;

public record TechBlogCommentCursorInfo(
    List<TechBlogCommentInfo> content,
    Long nextCursor,
    boolean hasNext
) {
}
