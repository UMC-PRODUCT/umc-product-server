package com.umc.product.blog.application.port.in.query.dto;

public record BlogContentListQuery(
    String type,
    String seriesSlug,
    String hashtagSlug,
    Long cursor,
    int size,
    String sort,
    Long viewerMemberId
) {
    public static BlogContentListQuery of(
        String type,
        String seriesSlug,
        String hashtagSlug,
        Long cursor,
        int size,
        String sort,
        Long viewerMemberId
    ) {
        return new BlogContentListQuery(type, seriesSlug, hashtagSlug, cursor, size, sort, viewerMemberId);
    }
}
