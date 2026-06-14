package com.umc.product.blog.domain;

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
@Table(name = "blog_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogComment extends BaseEntity {

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

    @Column(length = 20)
    private String nickname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "deletion_type", nullable = false, length = 30)
    private BlogCommentDeletionType deletionType;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by_member_id")
    private Long deletedByMemberId;

    private BlogComment(
        Long contentId,
        Long parentCommentId,
        Long authorMemberId,
        boolean anonymous,
        String nickname,
        String content
    ) {
        this.contentId = contentId;
        this.parentCommentId = parentCommentId;
        this.authorMemberId = authorMemberId;
        this.anonymous = anonymous;
        this.nickname = nickname;
        this.content = content;
        this.deletionType = BlogCommentDeletionType.NONE;
    }

    public static BlogComment create(
        Long contentId,
        Long parentCommentId,
        Long authorMemberId,
        boolean anonymous,
        String nickname,
        String content
    ) {
        validateContentId(contentId);
        validateContent(content);
        if (parentCommentId != null && parentCommentId <= 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_PARENT_COMMENT);
        }
        String normalizedNickname = normalizeNickname(nickname);
        if (authorMemberId == null) {
            validateNickname(normalizedNickname);
            anonymous = true;
        } else if (normalizedNickname != null && normalizedNickname.length() > 20) {
            throw new BlogDomainException(BlogErrorCode.INVALID_NICKNAME);
        }

        return new BlogComment(
            contentId,
            parentCommentId,
            authorMemberId,
            anonymous,
            normalizedNickname,
            content.trim()
        );
    }

    public void updateContent(String content) {
        ensureNotDeleted();
        validateContent(content);
        this.content = content.trim();
    }

    public void deleteByUser(Long memberId) {
        markDeleted(memberId, BlogCommentDeletionType.USER_DELETED);
    }

    public void deleteByAdmin(Long memberId) {
        markDeleted(memberId, BlogCommentDeletionType.ADMIN_DELETED);
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
            throw new BlogDomainException(BlogErrorCode.COMMENT_ALREADY_DELETED);
        }
    }

    private void markDeleted(Long memberId, BlogCommentDeletionType deletionType) {
        if (memberId == null || memberId <= 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_MEMBER_ID);
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
            throw new BlogDomainException(BlogErrorCode.INVALID_ID);
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank() || content.trim().length() > 1000) {
            throw new BlogDomainException(BlogErrorCode.INVALID_COMMENT_CONTENT);
        }
    }

    private static void validateNickname(String nickname) {
        String normalized = normalizeNickname(nickname);
        if (normalized == null || normalized.length() > 20) {
            throw new BlogDomainException(BlogErrorCode.INVALID_NICKNAME);
        }
    }

    private static String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return null;
        }
        return nickname.trim();
    }
}
