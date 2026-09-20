package com.umc.product.blog.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.umc.product.blog.domain.BlogContentType;
import com.umc.product.blog.domain.BlogSeries;
import com.umc.product.blog.domain.BlogSeriesSort;

public interface LoadBlogSeriesPort {

    Optional<BlogSeries> findSeriesById(Long seriesId);

    Optional<BlogSeries> findSeriesByTypeAndSlug(BlogContentType type, String slug);

    List<BlogSeries> listPublicSeries(BlogContentType type, BlogSeriesSort sort, Long cursor, int limit);

    List<BlogSeries> listSeriesByContentIds(List<Long> contentIds);

    Map<Long, Integer> countPublishedContentsBySeriesIds(List<Long> seriesIds);

    Map<Long, List<BlogSeries>> listSeriesByContentIdsGrouped(List<Long> contentIds);

    boolean existsSeriesByTypeAndSlug(BlogContentType type, String slug, Long excludedSeriesId);

    boolean hasPublishedContent(Long seriesId);
}
