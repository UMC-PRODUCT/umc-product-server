package com.umc.product.blog.domain;

public enum BlogSeriesSort {
    CREATED_AT_DESC(false),
    CREATED_AT_ASC(true),
    DISPLAY_ORDER_ASC(true);

    private final boolean ascending;

    BlogSeriesSort(boolean ascending) {
        this.ascending = ascending;
    }

    public static BlogSeriesSort fromSeriesList(String value) {
        String normalized = value == null || value.isBlank() ? "createdAt,desc" : value.trim();
        return switch (normalized) {
            case "createdAt,desc" -> CREATED_AT_DESC;
            case "createdAt,asc" -> CREATED_AT_ASC;
            default -> throw new BlogDomainException(BlogErrorCode.INVALID_SORT);
        };
    }

    public static BlogSeriesSort fromSeriesContents(String value) {
        String normalized = value == null || value.isBlank() ? "displayOrder,asc" : value.trim();
        return switch (normalized) {
            case "displayOrder,asc" -> DISPLAY_ORDER_ASC;
            default -> throw new BlogDomainException(BlogErrorCode.INVALID_SORT);
        };
    }

    public boolean isAscending() {
        return ascending;
    }
}
