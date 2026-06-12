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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "blog_series",
    uniqueConstraints = @UniqueConstraint(name = "uk_blog_series_type_slug", columnNames = {"content_type", "slug"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogSeries extends BaseEntity {

    private static final int MAX_SLUG_LENGTH = 200;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int MAX_URL_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private BlogContentType contentType;

    @Column(nullable = false, length = 200)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "author_member_id", nullable = false)
    private Long authorMemberId;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by_member_id")
    private Long deletedByMemberId;

    @Column(name = "seo_title", length = 200)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "og_image_url", length = 1000)
    private String ogImageUrl;

    private BlogSeries(
        BlogContentType contentType,
        String slug,
        String title,
        String description,
        String thumbnailUrl,
        Long authorMemberId,
        String seoTitle,
        String seoDescription,
        String ogImageUrl
    ) {
        this.contentType = contentType;
        this.slug = slug.trim();
        this.title = title.trim();
        this.description = normalizeOptional(description);
        this.thumbnailUrl = normalizeOptional(thumbnailUrl);
        this.authorMemberId = authorMemberId;
        this.seoTitle = normalizeOptional(seoTitle);
        this.seoDescription = normalizeOptional(seoDescription);
        this.ogImageUrl = normalizeOptional(ogImageUrl);
    }

    public static BlogSeries create(
        BlogContentType type,
        String slug,
        String title,
        String description,
        String thumbnailUrl,
        Long authorMemberId,
        String seoTitle,
        String seoDescription,
        String ogImageUrl
    ) {
        validate(type, slug, title, description, thumbnailUrl, authorMemberId, seoTitle, seoDescription, ogImageUrl);
        return new BlogSeries(type, slug, title, description, thumbnailUrl, authorMemberId, seoTitle, seoDescription,
            ogImageUrl);
    }

    public void update(
        String slug,
        String title,
        String description,
        String thumbnailUrl,
        String seoTitle,
        String seoDescription,
        String ogImageUrl
    ) {
        ensureNotDeleted();
        validate(contentType, slug, title, description, thumbnailUrl, authorMemberId, seoTitle, seoDescription,
            ogImageUrl);
        this.slug = slug.trim();
        this.title = title.trim();
        this.description = normalizeOptional(description);
        this.thumbnailUrl = normalizeOptional(thumbnailUrl);
        this.seoTitle = normalizeOptional(seoTitle);
        this.seoDescription = normalizeOptional(seoDescription);
        this.ogImageUrl = normalizeOptional(ogImageUrl);
    }

    public void softDelete(Long deletedByMemberId) {
        ensureMemberId(deletedByMemberId);
        if (isDeleted()) {
            return;
        }
        this.deletedAt = Instant.now();
        this.deletedByMemberId = deletedByMemberId;
    }

    public boolean isAuthor(Long memberId) {
        return memberId != null && memberId.equals(authorMemberId);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public String canonicalPath() {
        return "/series/" + contentType.name().toLowerCase() + "/" + slug;
    }

    public String resolvedSeoTitle() {
        return seoTitle == null ? title : seoTitle;
    }

    public String resolvedSeoDescription() {
        return seoDescription == null ? description : seoDescription;
    }

    public String resolvedOgImageUrl() {
        return ogImageUrl == null ? thumbnailUrl : ogImageUrl;
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BlogDomainException(BlogErrorCode.SERIES_ALREADY_DELETED);
        }
    }

    private static void validate(
        BlogContentType type,
        String slug,
        String title,
        String description,
        String thumbnailUrl,
        Long authorMemberId,
        String seoTitle,
        String seoDescription,
        String ogImageUrl
    ) {
        if (type == null) {
            throw new BlogDomainException(BlogErrorCode.INVALID_CONTENT_TYPE);
        }
        if (slug == null || slug.isBlank() || slug.trim().length() > MAX_SLUG_LENGTH) {
            throw new BlogDomainException(BlogErrorCode.INVALID_SLUG);
        }
        if (title == null || title.isBlank() || title.trim().length() > MAX_TITLE_LENGTH) {
            throw new BlogDomainException(BlogErrorCode.INVALID_SERIES_TITLE);
        }
        if (description != null && description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            throw new BlogDomainException(BlogErrorCode.INVALID_SERIES_DESCRIPTION);
        }
        if (thumbnailUrl != null && thumbnailUrl.trim().length() > MAX_URL_LENGTH) {
            throw new BlogDomainException(BlogErrorCode.INVALID_THUMBNAIL_URL);
        }
        ensureMemberId(authorMemberId);
        if (seoTitle != null && seoTitle.trim().length() > MAX_TITLE_LENGTH) {
            throw new BlogDomainException(BlogErrorCode.INVALID_SERIES_TITLE);
        }
        if (seoDescription != null && seoDescription.trim().length() > 500) {
            throw new BlogDomainException(BlogErrorCode.INVALID_SERIES_DESCRIPTION);
        }
        if (ogImageUrl != null && ogImageUrl.trim().length() > MAX_URL_LENGTH) {
            throw new BlogDomainException(BlogErrorCode.INVALID_THUMBNAIL_URL);
        }
    }

    private static void ensureMemberId(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_MEMBER_ID);
        }
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
