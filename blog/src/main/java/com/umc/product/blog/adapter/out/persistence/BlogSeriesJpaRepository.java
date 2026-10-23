package com.umc.product.blog.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.blog.domain.BlogContentType;
import com.umc.product.blog.domain.BlogSeries;

public interface BlogSeriesJpaRepository extends JpaRepository<BlogSeries, Long> {

    Optional<BlogSeries> findByContentTypeAndSlug(BlogContentType contentType, String slug);

    boolean existsByContentTypeAndSlugAndIdNot(BlogContentType contentType, String slug, Long id);

    boolean existsByContentTypeAndSlug(BlogContentType contentType, String slug);
}
