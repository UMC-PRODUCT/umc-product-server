package com.umc.product.techblog.domain;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TechBlogComment {

    private final Long id;
    private final Long contentId;
    private final Long parentCommentId;
    private final Long authorMemberId;
    private final boolean anonymous;
    private final String guestNickname;
    private final Instant createdAt;
    private String content;
    private TechBlogCommentDeletionType deletionType;
    private Instant deletedAt;
    private Long deletedByMemberId;

    public static TechBlogComment create(
        Long contentId,
        Long parentCommentId,
        Long authorMemberId,
        boolean anonymous,
        String guestNickname,
        String content
    ) {
        validateContentId(contentId);
        validateContent(content);
        if (parentCommentId != null && parentCommentId <= 0) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_PARENT_COMMENT);
        }
        if (authorMemberId == null) {
            validateGuestNickname(guestNickname);
            anonymous = true;
        }

        return TechBlogComment.builder()
            .contentId(contentId)
            .parentCommentId(parentCommentId)
            .authorMemberId(authorMemberId)
            .anonymous(anonymous)
            .guestNickname(normalizeGuestNickname(guestNickname))
            .content(content.trim())
            .deletionType(TechBlogCommentDeletionType.NONE)
            .build();
    }

    public static TechBlogComment reconstruct(
        Long id,
        Long contentId,
        Long parentCommentId,
        Long authorMemberId,
        boolean anonymous,
        String guestNickname,
        String content,
        TechBlogCommentDeletionType deletionType,
        Instant deletedAt,
        Long deletedByMemberId,
        Instant createdAt
    ) {
        if (id == null || id <= 0) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_ID);
        }
        validateContentId(contentId);
        validateContent(content);
        return TechBlogComment.builder()
            .id(id)
            .contentId(contentId)
            .parentCommentId(parentCommentId)
            .authorMemberId(authorMemberId)
            .anonymous(anonymous)
            .guestNickname(guestNickname)
            .content(content)
            .deletionType(deletionType == null ? TechBlogCommentDeletionType.NONE : deletionType)
            .deletedAt(deletedAt)
            .deletedByMemberId(deletedByMemberId)
            .createdAt(createdAt)
            .build();
    }

    public void updateContent(String content) {
        ensureNotDeleted();
        validateContent(content);
        this.content = content.trim();
    }

    public void deleteByUser(Long memberId) {
        markDeleted(memberId, TechBlogCommentDeletionType.USER_DELETED);
    }

    public void deleteByAdmin(Long memberId) {
        markDeleted(memberId, TechBlogCommentDeletionType.ADMIN_DELETED);
    }

    public boolean isDeleted() {
        return deletionType != null && deletionType.isDeleted();
    }

    public boolean canReply() {
        return parentCommentId == null && !isDeleted();
    }

    public String displayContent() {
        return isDeleted() ? deletionType.placeholderContent() : content;
    }

    public void ensureNotDeleted() {
        if (isDeleted()) {
            throw new TechBlogDomainException(TechBlogErrorCode.COMMENT_ALREADY_DELETED);
        }
    }

    private void markDeleted(Long memberId, TechBlogCommentDeletionType deletionType) {
        if (memberId == null || memberId <= 0) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_MEMBER_ID);
        }
        if (isDeleted()) {
            return;
        }
        this.deletionType = deletionType;
        this.deletedAt = Instant.now();
        this.deletedByMemberId = memberId;
    }

    private static void validateContentId(Long contentId) {
        if (contentId == null || contentId <= 0) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_ID);
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank() || content.trim().length() > 1000) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_COMMENT_CONTENT);
        }
    }

    private static void validateGuestNickname(String guestNickname) {
        String normalized = normalizeGuestNickname(guestNickname);
        if (normalized == null || normalized.length() > 20) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_GUEST_NICKNAME);
        }
    }

    private static String normalizeGuestNickname(String guestNickname) {
        if (guestNickname == null || guestNickname.isBlank()) {
            return null;
        }
        return guestNickname.trim();
    }
}
