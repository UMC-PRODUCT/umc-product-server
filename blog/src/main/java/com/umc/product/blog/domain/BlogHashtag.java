package com.umc.product.blog.domain;

import java.text.Normalizer;
import java.util.Locale;

import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "blog_hashtag",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_blog_hashtag_normalized_name", columnNames = "normalized_name"),
        @UniqueConstraint(name = "uk_blog_hashtag_slug", columnNames = "slug")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogHashtag extends BaseEntity {

    private static final int MAX_NAME_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 30)
    private String normalizedName;

    @Column(nullable = false, length = 30)
    private String slug;

    private BlogHashtag(String name, String normalizedName, String slug) {
        this.name = name;
        this.normalizedName = normalizedName;
        this.slug = slug;
    }

    public static BlogHashtag create(String name) {
        String normalized = normalize(name);
        return new BlogHashtag(displayName(name), normalized, normalized);
    }

    public static String normalize(String value) {
        String displayName = displayName(value);
        String normalized = Normalizer.normalize(displayName, Normalizer.Form.NFKC)
            .toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > MAX_NAME_LENGTH || normalized.chars().anyMatch(Character::isWhitespace)) {
            throw new BlogDomainException(BlogErrorCode.INVALID_HASHTAG);
        }
        return normalized;
    }

    private static String displayName(String value) {
        if (value == null) {
            throw new BlogDomainException(BlogErrorCode.INVALID_HASHTAG);
        }
        String trimmed = value.trim();
        while (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.isBlank() || trimmed.length() > MAX_NAME_LENGTH || trimmed.chars().anyMatch(Character::isWhitespace)) {
            throw new BlogDomainException(BlogErrorCode.INVALID_HASHTAG);
        }
        return trimmed;
    }
}
