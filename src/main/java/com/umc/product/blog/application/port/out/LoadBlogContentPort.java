package com.umc.product.blog.application.port.out;

import java.util.List;
import java.util.Optional;

import com.umc.product.blog.domain.BlogContent;
import com.umc.product.blog.domain.BlogContentSort;
import com.umc.product.blog.domain.BlogContentType;

public interface LoadBlogContentPort {

    Optional<BlogContent> findContentById(Long contentId);

    Optional<BlogContent> findByTypeAndSlug(BlogContentType type, String slug);

    Optional<BlogContent> findPublishedByTypeAndSlug(BlogContentType type, String slug);

    List<BlogContent> listPublicContents(
        BlogContentType type,
        String seriesSlug,
        String hashtagSlug,
        BlogContentSort sort,
        Long cursor,
        int limit
    );

    List<BlogContent> listPublicSeriesContents(Long seriesId, Long cursor, int limit);

    List<BlogContent> listPublicHashtagContents(Long hashtagId, BlogContentType type, BlogContentSort sort, Long cursor,
                                                int limit);

    boolean existsContentByTypeAndSlug(BlogContentType type, String slug, Long excludedContentId);

    List<BlogContent> listByIds(List<Long> contentIds);
}
