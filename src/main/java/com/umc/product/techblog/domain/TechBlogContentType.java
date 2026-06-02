package com.umc.product.techblog.domain;

public enum TechBlogContentType {
    BLOG,
    RELEASE;

    public static TechBlogContentType fromPath(String value) {
        if (value == null || value.isBlank()) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_CONTENT_TYPE);
        }

        return switch (value.trim().toLowerCase()) {
            case "blog" -> BLOG;
            case "release" -> RELEASE;
            default -> throw new TechBlogDomainException(TechBlogErrorCode.INVALID_CONTENT_TYPE);
        };
    }
}
