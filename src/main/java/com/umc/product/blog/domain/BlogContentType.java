package com.umc.product.blog.domain;

import java.util.Locale;

public enum BlogContentType {
    ENGINEERING,
    DESIGN,
    PRODUCT,
    RELEASE;

    public static BlogContentType fromPath(String value) {
        if (value == null || value.isBlank()) {
            throw new BlogDomainException(BlogErrorCode.INVALID_CONTENT_TYPE);
        }

        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "engineering" -> ENGINEERING;
            case "design" -> DESIGN;
            case "product" -> PRODUCT;
            case "release" -> RELEASE;
            default -> throw new BlogDomainException(BlogErrorCode.INVALID_CONTENT_TYPE);
        };
    }
}
