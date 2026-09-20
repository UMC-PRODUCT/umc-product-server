package com.umc.product.blog.domain;

import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "blog_comment_like")
@IdClass(BlogCommentLikeId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogCommentLike extends BaseEntity {

    @Id
    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    private BlogCommentLike(Long commentId, Long memberId) {
        this.commentId = commentId;
        this.memberId = memberId;
    }

    public static BlogCommentLike create(Long commentId, Long memberId) {
        validateCommentId(commentId);
        validateMemberId(memberId);
        return new BlogCommentLike(commentId, memberId);
    }

    private static void validateCommentId(Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_ID);
        }
    }

    private static void validateMemberId(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_MEMBER_ID);
        }
    }
}
