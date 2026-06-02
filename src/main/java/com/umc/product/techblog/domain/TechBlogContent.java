package com.umc.product.techblog.domain;

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
    name = "tech_blog_content",
    uniqueConstraints = @UniqueConstraint(name = "uk_tech_blog_content_type_slug", columnNames = {"content_type", "slug"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechBlogContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private TechBlogContentType contentType;

    @Column(nullable = false, length = 200)
    private String slug;

    private TechBlogContent(TechBlogContentType contentType, String slug) {
        this.contentType = contentType;
        this.slug = slug;
    }

    public static TechBlogContent create(TechBlogContentType type, String slug) {
        validate(type, slug);
        return new TechBlogContent(type, slug.trim());
    }

    public TechBlogContentType getType() {
        return contentType;
    }

    private static void validate(TechBlogContentType type, String slug) {
        if (type == null) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_CONTENT_TYPE);
        }
        if (slug == null || slug.isBlank() || slug.length() > 200) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_SLUG);
        }
    }
}
