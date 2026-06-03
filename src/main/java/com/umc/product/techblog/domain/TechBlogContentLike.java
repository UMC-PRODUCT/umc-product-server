package com.umc.product.techblog.domain;

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
@Table(name = "tech_blog_content_like")
@IdClass(TechBlogContentLikeId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechBlogContentLike extends BaseEntity {

    @Id
    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    private TechBlogContentLike(Long contentId, Long memberId) {
        this.contentId = contentId;
        this.memberId = memberId;
    }

    public static TechBlogContentLike create(Long contentId, Long memberId) {
        validateContentId(contentId);
        validateMemberId(memberId);
        return new TechBlogContentLike(contentId, memberId);
    }

    private static void validateContentId(Long contentId) {
        if (contentId == null || contentId <= 0) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_ID);
        }
    }

    private static void validateMemberId(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_MEMBER_ID);
        }
    }
}
