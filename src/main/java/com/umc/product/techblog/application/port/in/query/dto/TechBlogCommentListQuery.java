package com.umc.product.techblog.application.port.in.query.dto;

public record TechBlogCommentListQuery(
    String type,
    String slug,
    Long cursor,
    int size,
    String sort,
    Long viewerMemberId
) {

    public static TechBlogCommentListQuery of(
        String type,
        String slug,
        Long cursor,
        int size,
        String sort,
        Long viewerMemberId
    ) {
        return new TechBlogCommentListQuery(type, slug, cursor, size, sort, viewerMemberId);
    }
}
