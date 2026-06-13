package com.umc.product.blog.domain;

import java.util.Locale;

public enum BlogCommentSort {
    CREATED_AT_DESC(false),
    CREATED_AT_ASC(true);

    private final boolean ascending;

    BlogCommentSort(boolean ascending) {
        this.ascending = ascending;
    }

    public static BlogCommentSort from(String value) {
        if (value == null || value.isBlank()) {
            return CREATED_AT_DESC;
        }

        // Java 기본 Locale이 터키어면 'I'가 점 없는 'ı'로 바뀔 수 있어 정렬 키는 Locale.ROOT로 고정한다.
        String normalized = value.trim().replace(" ", "").toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "createdat,desc" -> CREATED_AT_DESC;
            case "createdat,asc" -> CREATED_AT_ASC;
            default -> throw new BlogDomainException(BlogErrorCode.INVALID_COMMENT_SORT);
        };
    }

    public boolean isAscending() {
        return ascending;
    }
}
