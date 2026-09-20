package com.umc.product.blog.domain;

public enum BlogContentStatus {
    DRAFT,
    PUBLISHED,
    DELETED;

    public boolean isPublished() {
        return this == PUBLISHED;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}
