package com.umc.product.blog.domain;

public enum BlogHashtagSort {
    CONTENT_COUNT_DESC;

    public static BlogHashtagSort from(String value) {
        String normalized = value == null || value.isBlank() ? "contentCount,desc" : value.trim();
        return switch (normalized) {
            case "contentCount,desc" -> CONTENT_COUNT_DESC;
            default -> throw new BlogDomainException(BlogErrorCode.INVALID_SORT);
        };
    }
}
