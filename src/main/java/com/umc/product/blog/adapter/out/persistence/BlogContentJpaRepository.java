package com.umc.product.blog.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.blog.domain.BlogContent;
import com.umc.product.blog.domain.BlogContentStatus;
import com.umc.product.blog.domain.BlogContentType;

public interface BlogContentJpaRepository extends JpaRepository<BlogContent, Long> {

    Optional<BlogContent> findByContentTypeAndSlug(BlogContentType contentType, String slug);

    Optional<BlogContent> findByContentTypeAndSlugAndStatus(BlogContentType contentType, String slug,
                                                            BlogContentStatus status);

    boolean existsByContentTypeAndSlugAndIdNot(BlogContentType contentType, String slug, Long id);

    boolean existsByContentTypeAndSlug(BlogContentType contentType, String slug);
}
