package com.umc.product.blog.application.port.in.query.dto;

public record BlogSeriesListQuery(
    String type,
    Long cursor,
    int size,
    String sort,
    Long viewerMemberId
) {
    public static BlogSeriesListQuery of(String type, Long cursor, int size, String sort, Long viewerMemberId) {
        return new BlogSeriesListQuery(type, cursor, size, sort, viewerMemberId);
    }
}
