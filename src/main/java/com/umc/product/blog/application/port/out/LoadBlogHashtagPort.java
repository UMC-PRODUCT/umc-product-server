package com.umc.product.blog.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.umc.product.blog.domain.BlogContentType;
import com.umc.product.blog.domain.BlogHashtag;
import com.umc.product.blog.domain.BlogHashtagSort;

public interface LoadBlogHashtagPort {

    Optional<BlogHashtag> findBySlug(String slug);

    Optional<BlogHashtag> findByNormalizedName(String normalizedName);

    List<BlogHashtag> listPublicHashtags(BlogContentType type, String q, BlogHashtagSort sort, Long cursor, int limit);

    List<BlogHashtag> listHashtagsByContentIds(List<Long> contentIds);

    Map<Long, List<BlogHashtag>> listHashtagsByContentIdsGrouped(List<Long> contentIds);

    Map<Long, Integer> countPublishedContentsByHashtagIds(List<Long> hashtagIds);
}
