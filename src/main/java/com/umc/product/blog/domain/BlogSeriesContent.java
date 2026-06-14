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
@Table(name = "blog_series_content")
@IdClass(BlogSeriesContentId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogSeriesContent extends BaseEntity {

    @Id
    @Column(name = "series_id", nullable = false)
    private Long seriesId;

    @Id
    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    private BlogSeriesContent(Long seriesId, Long contentId, int displayOrder) {
        this.seriesId = seriesId;
        this.contentId = contentId;
        this.displayOrder = displayOrder;
    }

    public static BlogSeriesContent create(Long seriesId, Long contentId, int displayOrder) {
        validateId(seriesId);
        validateId(contentId);
        if (displayOrder < 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_DISPLAY_ORDER);
        }
        return new BlogSeriesContent(seriesId, contentId, displayOrder);
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_ID);
        }
    }
}
