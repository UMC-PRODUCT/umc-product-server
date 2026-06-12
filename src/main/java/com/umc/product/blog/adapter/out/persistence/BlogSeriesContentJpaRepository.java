package com.umc.product.blog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.blog.domain.BlogSeriesContent;
import com.umc.product.blog.domain.BlogSeriesContentId;

public interface BlogSeriesContentJpaRepository extends JpaRepository<BlogSeriesContent, BlogSeriesContentId> {

    void deleteBySeriesId(Long seriesId);
}
