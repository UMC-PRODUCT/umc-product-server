package com.umc.product.techblog.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentType;

public interface TechBlogContentJpaRepository extends JpaRepository<TechBlogContent, Long> {

    Optional<TechBlogContent> findByContentTypeAndSlug(TechBlogContentType contentType, String slug);

    @Modifying
    @Query(value = """
        INSERT INTO tech_blog_content (content_type, slug, created_at, updated_at)
        VALUES (:contentType, :slug, now(), now())
        ON CONFLICT (content_type, slug) DO NOTHING
        """, nativeQuery = true)
    int insertIgnore(
        @Param("contentType") String contentType,
        @Param("slug") String slug
    );
}
