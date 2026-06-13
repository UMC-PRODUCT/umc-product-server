package com.umc.product.blog.domain;

public enum BlogContentSort {
    PUBLISHED_AT_DESC(false),
    PUBLISHED_AT_ASC(true);

    private final boolean ascending;

    BlogContentSort(boolean ascending) {
        this.ascending = ascending;
    }

    public static BlogContentSort from(String value) {
        String normalized = value == null || value.isBlank() ? "publishedAt,desc" : value.trim();
        return switch (normalized) {
            case "publishedAt,desc" -> PUBLISHED_AT_DESC;
            case "publishedAt,asc" -> PUBLISHED_AT_ASC;
            default -> throw new BlogDomainException(BlogErrorCode.INVALID_SORT);
        };
    }

    public boolean isAscending() {
        return ascending;
    }
}
