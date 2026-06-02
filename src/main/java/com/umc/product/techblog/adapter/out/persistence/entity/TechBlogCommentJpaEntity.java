package com.umc.product.techblog.adapter.out.persistence.entity;

import com.umc.product.common.BaseEntity;
import com.umc.product.techblog.domain.TechBlogComment;
import com.umc.product.techblog.domain.TechBlogCommentDeletionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tech_blog_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechBlogCommentJpaEntity extends BaseEntity {

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

    private TechBlogCommentJpaEntity(
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

    public static TechBlogCommentJpaEntity from(TechBlogComment comment) {
        return new TechBlogCommentJpaEntity(
            comment.getContentId(),
            comment.getParentCommentId(),
            comment.getAuthorMemberId(),
            comment.isAnonymous(),
            comment.getGuestNickname(),
            comment.getContent()
        );
    }

    public TechBlogComment toDomain() {
        return TechBlogComment.reconstruct(
            id,
            contentId,
            parentCommentId,
            authorMemberId,
            anonymous,
            guestNickname,
            content,
            deletionType,
            deletedAt,
            deletedByMemberId,
            getCreatedAt()
        );
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete(Long deletedByMemberId, boolean admin) {
        this.deletionType = admin
            ? TechBlogCommentDeletionType.ADMIN_DELETED
            : TechBlogCommentDeletionType.USER_DELETED;
        this.deletedByMemberId = deletedByMemberId;
        this.deletedAt = Instant.now();
    }
}
