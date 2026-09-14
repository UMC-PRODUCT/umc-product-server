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

        return switch (normalizeIdentifier(value)) {
            case "engineering" -> ENGINEERING;
            case "design" -> DESIGN;
            case "product" -> PRODUCT;
            case "release" -> RELEASE;
            default -> throw new BlogDomainException(BlogErrorCode.INVALID_CONTENT_TYPE);
        };
    }

    public String pathValue() {
        return normalizeIdentifier(name());
    }

    private static String normalizeIdentifier(String value) {
        // Java 기본 Locale이 터키어면 'I'가 점 없는 'ı'로 바뀔 수 있어 기술 식별자는 Locale.ROOT로 고정한다.
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
