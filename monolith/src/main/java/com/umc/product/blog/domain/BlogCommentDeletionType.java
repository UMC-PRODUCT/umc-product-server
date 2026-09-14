package com.umc.product.blog.domain;

public enum BlogCommentDeletionType {
    NONE(null),
    USER_DELETED("삭제된 댓글이에요"),
    ADMIN_DELETED("관리자가 삭제한 댓글이에요");

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
