package com.umc.product.blog.adapter.in.web.dto.response;

import com.umc.product.blog.application.port.in.query.dto.BlogHashtagInfo;

public record BlogHashtagResponse(
    Long id,
    String name,
    String slug,
    int contentCount
) {
    public static BlogHashtagResponse from(BlogHashtagInfo info) {
        return new BlogHashtagResponse(info.id(), info.name(), info.slug(), info.contentCount());
    }
}
