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
@Table(name = "blog_content_hashtag")
@IdClass(BlogContentHashtagId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogContentHashtag extends BaseEntity {

    @Id
    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Id
    @Column(name = "hashtag_id", nullable = false)
    private Long hashtagId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    private BlogContentHashtag(Long contentId, Long hashtagId, int displayOrder) {
        this.contentId = contentId;
        this.hashtagId = hashtagId;
        this.displayOrder = displayOrder;
    }

    public static BlogContentHashtag create(Long contentId, Long hashtagId, int displayOrder) {
        validateId(contentId);
        validateId(hashtagId);
        if (displayOrder < 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_DISPLAY_ORDER);
        }
        return new BlogContentHashtag(contentId, hashtagId, displayOrder);
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_ID);
        }
    }
}
