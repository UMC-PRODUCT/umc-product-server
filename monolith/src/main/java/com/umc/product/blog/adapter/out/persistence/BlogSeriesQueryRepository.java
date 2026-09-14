package com.umc.product.blog.adapter.out.persistence;

import static com.umc.product.blog.domain.QBlogContent.blogContent;
import static com.umc.product.blog.domain.QBlogSeries.blogSeries;
import static com.umc.product.blog.domain.QBlogSeriesContent.blogSeriesContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.blog.domain.BlogContentStatus;
import com.umc.product.blog.domain.BlogContentType;
import com.umc.product.blog.domain.BlogDomainException;
import com.umc.product.blog.domain.BlogErrorCode;
import com.umc.product.blog.domain.BlogSeries;
import com.umc.product.blog.domain.BlogSeriesSort;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlogSeriesQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final BlogSeriesJpaRepository seriesJpaRepository;

    public List<BlogSeries> listPublicSeries(BlogContentType type, BlogSeriesSort sort, Long cursor, int limit) {
        BlogSeries cursorEntity = getCursorEntity(cursor);
        return queryFactory
            .selectFrom(blogSeries)
            .where(
                blogSeries.deletedAt.isNull(),
                type == null ? null : blogSeries.contentType.eq(type),
                hasPublishedContent(),
                cursorCondition(cursorEntity, sort)
            )
            .orderBy(orderSpecifiers(sort))
            .limit(limit)
            .fetch();
    }

    public List<BlogSeries> listByContentIds(List<Long> contentIds) {
        return listByContentIdsGrouped(contentIds).values().stream()
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    public Map<Long, List<BlogSeries>> listByContentIdsGrouped(List<Long> contentIds) {
        Map<Long, List<BlogSeries>> result = new HashMap<>();
        if (contentIds == null || contentIds.isEmpty()) {
            return result;
        }

        List<Tuple> rows = queryFactory
            .select(blogSeriesContent.contentId, blogSeries)
            .from(blogSeriesContent)
            .join(blogSeries).on(blogSeries.id.eq(blogSeriesContent.seriesId))
            .where(
                blogSeriesContent.contentId.in(contentIds),
                blogSeries.deletedAt.isNull()
            )
            .orderBy(blogSeriesContent.displayOrder.asc(), blogSeries.id.asc())
            .fetch();

        for (Tuple row : rows) {
            Long contentId = row.get(blogSeriesContent.contentId);
            BlogSeries series = row.get(blogSeries);
            result.computeIfAbsent(contentId, ignored -> new ArrayList<>()).add(series);
        }
        return result;
    }

    public Map<Long, Integer> countPublishedContentsBySeriesIds(List<Long> seriesIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (seriesIds == null || seriesIds.isEmpty()) {
            return result;
        }

        List<Tuple> rows = queryFactory
            .select(blogSeriesContent.seriesId, blogContent.id.countDistinct())
            .from(blogSeriesContent)
            .join(blogContent).on(blogContent.id.eq(blogSeriesContent.contentId))
            .where(
                blogSeriesContent.seriesId.in(seriesIds),
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull()
            )
            .groupBy(blogSeriesContent.seriesId)
            .fetch();

        for (Tuple row : rows) {
            Long seriesId = row.get(blogSeriesContent.seriesId);
            Long count = row.get(blogContent.id.countDistinct());
            result.put(seriesId, count == null ? 0 : count.intValue());
        }
        return result;
    }

    public boolean hasPublishedContent(Long seriesId) {
        return queryFactory
            .selectOne()
            .from(blogSeriesContent)
            .join(blogContent).on(blogContent.id.eq(blogSeriesContent.contentId))
            .where(
                blogSeriesContent.seriesId.eq(seriesId),
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull()
            )
            .fetchFirst() != null;
    }

    private BlogSeries getCursorEntity(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return seriesJpaRepository.findById(cursor)
            .orElseThrow(() -> new BlogDomainException(BlogErrorCode.INVALID_CURSOR));
    }

    private BooleanExpression hasPublishedContent() {
        return JPAExpressions
            .selectOne()
            .from(blogSeriesContent, blogContent)
            .where(
                blogSeriesContent.seriesId.eq(blogSeries.id),
                blogContent.id.eq(blogSeriesContent.contentId),
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull()
            )
            .exists();
    }

    private BooleanExpression cursorCondition(BlogSeries cursorEntity, BlogSeriesSort sort) {
        if (cursorEntity == null) {
            return null;
        }
        return sort.isAscending()
            ? blogSeries.createdAt.gt(cursorEntity.getCreatedAt())
                .or(blogSeries.createdAt.eq(cursorEntity.getCreatedAt()).and(blogSeries.id.gt(cursorEntity.getId())))
            : blogSeries.createdAt.lt(cursorEntity.getCreatedAt())
                .or(blogSeries.createdAt.eq(cursorEntity.getCreatedAt()).and(blogSeries.id.lt(cursorEntity.getId())));
    }

    private OrderSpecifier<?>[] orderSpecifiers(BlogSeriesSort sort) {
        if (sort.isAscending()) {
            return new OrderSpecifier<?>[] {blogSeries.createdAt.asc(), blogSeries.id.asc()};
        }
        return new OrderSpecifier<?>[] {blogSeries.createdAt.desc(), blogSeries.id.desc()};
    }
}
