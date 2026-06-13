package com.umc.product.blog.adapter.out.persistence;

import static com.umc.product.blog.domain.QBlogContent.blogContent;
import static com.umc.product.blog.domain.QBlogContentHashtag.blogContentHashtag;
import static com.umc.product.blog.domain.QBlogHashtag.blogHashtag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.blog.domain.BlogContentStatus;
import com.umc.product.blog.domain.BlogContentType;
import com.umc.product.blog.domain.BlogDomainException;
import com.umc.product.blog.domain.BlogErrorCode;
import com.umc.product.blog.domain.BlogHashtag;
import com.umc.product.blog.domain.BlogHashtagSort;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlogHashtagQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<BlogHashtag> listPublicHashtags(
        BlogContentType type,
        String q,
        BlogHashtagSort sort,
        Long cursor,
        int limit
    ) {
        NumberExpression<Long> contentCount = blogContent.id.countDistinct();
        Long cursorCount = getCursorCount(type, q, cursor);

        return queryFactory
            .select(blogHashtag)
            .from(blogHashtag)
            .join(blogContentHashtag).on(blogContentHashtag.hashtagId.eq(blogHashtag.id))
            .join(blogContent).on(blogContent.id.eq(blogContentHashtag.contentId))
            .where(
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull(),
                type == null ? null : blogContent.contentType.eq(type),
                keywordCondition(q)
            )
            .groupBy(blogHashtag.id)
            .having(cursorCondition(contentCount, cursor, cursorCount))
            .orderBy(contentCount.desc(), blogHashtag.id.desc())
            .limit(limit)
            .fetch();
    }

    public Map<Long, List<BlogHashtag>> listByContentIdsGrouped(List<Long> contentIds) {
        Map<Long, List<BlogHashtag>> result = new HashMap<>();
        if (contentIds == null || contentIds.isEmpty()) {
            return result;
        }

        List<Tuple> rows = queryFactory
            .select(blogContentHashtag.contentId, blogHashtag)
            .from(blogContentHashtag)
            .join(blogHashtag).on(blogHashtag.id.eq(blogContentHashtag.hashtagId))
            .where(blogContentHashtag.contentId.in(contentIds))
            .orderBy(blogContentHashtag.displayOrder.asc(), blogHashtag.id.asc())
            .fetch();

        for (Tuple row : rows) {
            Long contentId = row.get(blogContentHashtag.contentId);
            BlogHashtag hashtag = row.get(blogHashtag);
            result.computeIfAbsent(contentId, ignored -> new ArrayList<>()).add(hashtag);
        }
        return result;
    }

    public List<BlogHashtag> listByContentIds(List<Long> contentIds) {
        return listByContentIdsGrouped(contentIds).values().stream()
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    public Map<Long, Integer> countPublishedContentsByHashtagIds(List<Long> hashtagIds) {
        return countPublishedContentsByHashtagIds(hashtagIds, null);
    }

    public Map<Long, Integer> countPublishedContentsByHashtagIds(List<Long> hashtagIds, BlogContentType type) {
        Map<Long, Integer> result = new HashMap<>();
        if (hashtagIds == null || hashtagIds.isEmpty()) {
            return result;
        }

        List<Tuple> rows = queryFactory
            .select(blogContentHashtag.hashtagId, blogContent.id.countDistinct())
            .from(blogContentHashtag)
            .join(blogContent).on(blogContent.id.eq(blogContentHashtag.contentId))
            .where(
                blogContentHashtag.hashtagId.in(hashtagIds),
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull(),
                type == null ? null : blogContent.contentType.eq(type)
            )
            .groupBy(blogContentHashtag.hashtagId)
            .fetch();

        for (Tuple row : rows) {
            Long hashtagId = row.get(blogContentHashtag.hashtagId);
            Long count = row.get(blogContent.id.countDistinct());
            result.put(hashtagId, count == null ? 0 : count.intValue());
        }
        return result;
    }

    private Long getCursorCount(BlogContentType type, String q, Long cursor) {
        if (cursor == null) {
            return null;
        }
        Long cursorCount = queryFactory
            .select(blogContent.id.countDistinct())
            .from(blogHashtag)
            .join(blogContentHashtag).on(blogContentHashtag.hashtagId.eq(blogHashtag.id))
            .join(blogContent).on(blogContent.id.eq(blogContentHashtag.contentId))
            .where(
                blogHashtag.id.eq(cursor),
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull(),
                type == null ? null : blogContent.contentType.eq(type),
                keywordCondition(q)
            )
            .fetchOne();
        if (cursorCount == null || cursorCount <= 0) {
            throw new BlogDomainException(BlogErrorCode.INVALID_CURSOR);
        }
        return cursorCount;
    }

    private BooleanExpression keywordCondition(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return blogHashtag.normalizedName.containsIgnoreCase(q.trim());
    }

    private BooleanExpression cursorCondition(NumberExpression<Long> count, Long cursor, Long cursorCount) {
        if (cursor == null) {
            return null;
        }
        return count.lt(cursorCount).or(count.eq(cursorCount).and(blogHashtag.id.lt(cursor)));
    }
}
