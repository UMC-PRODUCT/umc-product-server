package com.umc.product.techblog.adapter.out.persistence.entity;

import com.umc.product.common.BaseEntity;
import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentType;
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

@Entity
@Table(
    name = "tech_blog_content",
    uniqueConstraints = @UniqueConstraint(name = "uk_tech_blog_content_type_slug", columnNames = {"content_type", "slug"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechBlogContentJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private TechBlogContentType contentType;

    @Column(nullable = false, length = 200)
    private String slug;

    private TechBlogContentJpaEntity(TechBlogContentType contentType, String slug) {
        this.contentType = contentType;
        this.slug = slug;
    }

    public static TechBlogContentJpaEntity from(TechBlogContent content) {
        return new TechBlogContentJpaEntity(content.getType(), content.getSlug());
    }

    public TechBlogContent toDomain() {
        return TechBlogContent.reconstruct(id, contentType, slug);
    }
}
