package com.umc.product.techblog.domain;

import java.time.Instant;

import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tech_blog_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechBlogComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "author_member_id")
    private Long authorMemberId;

    @Column(nullable = false)
    private boolean anonymous;

    @Column(name = "guest_nickname", length = 20)
    private String guestNickname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "deletion_type", nullable = false, length = 30)
    private TechBlogCommentDeletionType deletionType;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by_member_id")
    private Long deletedByMemberId;

    private TechBlogComment(
        Long contentId,
        Long parentCommentId,
        Long authorMemberId,
        boolean anonymous,
        String guestNickname,
        String content
    ) {
        this.contentId = contentId;
        this.parentCommentId = parentCommentId;
        this.authorMemberId = authorMemberId;
        this.anonymous = anonymous;
        this.guestNickname = guestNickname;
        this.content = content;
        this.deletionType = TechBlogCommentDeletionType.NONE;
    }

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
        String normalizedGuestNickname = normalizeGuestNickname(guestNickname);
        if (authorMemberId == null) {
            validateGuestNickname(normalizedGuestNickname);
            anonymous = true;
        } else if (normalizedGuestNickname != null && normalizedGuestNickname.length() > 20) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_GUEST_NICKNAME);
        }

        return new TechBlogComment(
            contentId,
            parentCommentId,
            authorMemberId,
            anonymous,
            normalizedGuestNickname,
            content.trim()
        );
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
