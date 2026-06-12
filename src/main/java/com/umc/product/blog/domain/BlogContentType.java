package com.umc.product.blog.domain;

public enum BlogContentType {
    BLOG,
    RELEASE;

    public static BlogContentType fromPath(String value) {
        if (value == null || value.isBlank()) {
            throw new BlogDomainException(BlogErrorCode.INVALID_CONTENT_TYPE);
        }

        return switch (value.trim().toLowerCase()) {
            case "blog" -> BLOG;
            case "release" -> RELEASE;
            default -> throw new BlogDomainException(BlogErrorCode.INVALID_CONTENT_TYPE);
        };
    }
}
