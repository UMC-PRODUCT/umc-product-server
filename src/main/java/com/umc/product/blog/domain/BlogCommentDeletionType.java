package com.umc.product.blog.domain;

public enum BlogCommentDeletionType {
    NONE(null),
    USER_DELETED("삭제된 댓글입니다"),
    ADMIN_DELETED("관리자에 의해서 삭제된 댓글입니다");

    private final String placeholderContent;

    BlogCommentDeletionType(String placeholderContent) {
        this.placeholderContent = placeholderContent;
    }

    public String placeholderContent() {
        return placeholderContent;
    }

    public boolean isDeleted() {
        return this != NONE;
    }
}
