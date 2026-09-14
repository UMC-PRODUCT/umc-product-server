package com.umc.product.blog.adapter.in.web.dto.response;

import java.time.Instant;
import java.util.List;

import com.umc.product.blog.application.port.in.query.dto.BlogSeoPathInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogSeoPathsInfo;

public record BlogSeoPathsResponse(
    List<PathResponse> paths
) {
    public static BlogSeoPathsResponse from(BlogSeoPathsInfo info) {
        return new BlogSeoPathsResponse(info.paths().stream().map(PathResponse::from).toList());
    }

    public record PathResponse(
        String type,
        String path,
        Instant updatedAt
    ) {
        private static PathResponse from(BlogSeoPathInfo info) {
            return new PathResponse(info.type(), info.path(), info.updatedAt());
        }
    }
}
