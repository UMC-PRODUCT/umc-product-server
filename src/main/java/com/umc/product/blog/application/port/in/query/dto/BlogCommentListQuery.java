package com.umc.product.blog.application.port.in.query.dto;

public record BlogCommentListQuery(
    String type,
    String slug,
    Long cursor,
    int size,
    String sort,
    Long viewerMemberId
) {

    public static BlogCommentListQuery of(
        String type,
        String slug,
        Long cursor,
        int size,
        String sort,
        Long viewerMemberId
    ) {
        return new BlogCommentListQuery(type, slug, cursor, size, sort, viewerMemberId);
    }
}
