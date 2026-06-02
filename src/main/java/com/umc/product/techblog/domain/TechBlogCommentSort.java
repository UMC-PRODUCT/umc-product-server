package com.umc.product.techblog.domain;

public enum TechBlogCommentSort {
    CREATED_AT_DESC(false),
    CREATED_AT_ASC(true);

    private final boolean ascending;

    TechBlogCommentSort(boolean ascending) {
        this.ascending = ascending;
    }

    public static TechBlogCommentSort from(String value) {
        if (value == null || value.isBlank()) {
            return CREATED_AT_DESC;
        }

        String normalized = value.trim().replace(" ", "").toLowerCase();
        return switch (normalized) {
            case "createdat,desc" -> CREATED_AT_DESC;
            case "createdat,asc" -> CREATED_AT_ASC;
            default -> throw new TechBlogDomainException(TechBlogErrorCode.INVALID_COMMENT_SORT);
        };
    }

    public boolean isAscending() {
        return ascending;
    }
}
