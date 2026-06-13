package com.umc.product.blog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.blog.domain.BlogSeriesContent;
import com.umc.product.blog.domain.BlogSeriesContentId;

public interface BlogSeriesContentJpaRepository extends JpaRepository<BlogSeriesContent, BlogSeriesContentId> {

    @Modifying
    @Query("DELETE FROM BlogSeriesContent b WHERE b.seriesId = :seriesId")
    void deleteBySeriesId(@Param("seriesId") Long seriesId);
}
