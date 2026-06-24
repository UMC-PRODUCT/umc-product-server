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
@Table(name = "blog_content_like")
@IdClass(BlogContentLikeId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogContentLike extends BaseEntity {

    @Id
    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    private BlogContentLike(Long contentId, Long memberId) {
        this.contentId = contentId;
        this.memberId = memberId;
    }

    public static BlogContentLike create(Long contentId, Long memberId) {
        validateContentId(contentId);
        validateMemberId(memberId);
        return new BlogContentLike(contentId, memberId);
    }

    private static void validateContentId(Long contentId) {
        if (contentId == null || contentId <= 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_ID);
        }
    }

    private static void validateMemberId(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_MEMBER_ID);
        }
    }
}
