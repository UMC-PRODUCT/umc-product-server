package com.umc.product.blog.adapter.out.persistence;

import static com.umc.product.blog.domain.QBlogContent.blogContent;
import static com.umc.product.blog.domain.QBlogContentHashtag.blogContentHashtag;
import static com.umc.product.blog.domain.QBlogHashtag.blogHashtag;
import static com.umc.product.blog.domain.QBlogSeries.blogSeries;
import static com.umc.product.blog.domain.QBlogSeriesContent.blogSeriesContent;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.blog.domain.BlogContent;
import com.umc.product.blog.domain.BlogContentSort;
import com.umc.product.blog.domain.BlogContentStatus;
import com.umc.product.blog.domain.BlogContentType;
import com.umc.product.blog.domain.BlogDomainException;
import com.umc.product.blog.domain.BlogErrorCode;
import com.umc.product.blog.domain.BlogSeriesContent;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlogContentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<BlogContent> listPublicContents(
        BlogContentType type,
        String seriesSlug,
        String hashtagSlug,
        BlogContentSort sort,
        Long cursor,
        int limit
    ) {
        BlogContent cursorEntity = getPublicContentCursor(type, seriesSlug, hashtagSlug, cursor);
        return queryFactory
            .selectFrom(blogContent)
            .where(
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull(),
                type == null ? null : blogContent.contentType.eq(type),
                seriesFilter(type, seriesSlug),
                hashtagFilter(hashtagSlug),
                cursorCondition(cursorEntity, sort)
            )
            .orderBy(orderSpecifiers(sort))
            .limit(limit)
            .fetch();
    }

    public List<BlogContent> listPublicSeriesContents(Long seriesId, Long cursor, int limit) {
        BlogSeriesContent cursorRelation = getSeriesContentCursor(seriesId, cursor);
        return queryFactory
            .select(blogContent)
            .from(blogSeriesContent)
            .join(blogContent).on(blogContent.id.eq(blogSeriesContent.contentId))
            .where(
                blogSeriesContent.seriesId.eq(seriesId),
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull(),
                seriesContentCursorCondition(cursorRelation)
            )
            .orderBy(blogSeriesContent.displayOrder.asc(), blogContent.id.asc())
            .limit(limit)
            .fetch();
    }

    public List<BlogContent> listPublicHashtagContents(
        Long hashtagId,
        BlogContentType type,
        BlogContentSort sort,
        Long cursor,
        int limit
    ) {
        BlogContent cursorEntity = getPublicHashtagContentCursor(hashtagId, type, cursor);
        return queryFactory
            .select(blogContent)
            .from(blogContentHashtag)
            .join(blogContent).on(blogContent.id.eq(blogContentHashtag.contentId))
            .where(
                blogContentHashtag.hashtagId.eq(hashtagId),
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull(),
                type == null ? null : blogContent.contentType.eq(type),
                cursorCondition(cursorEntity, sort)
            )
            .orderBy(orderSpecifiers(sort))
            .limit(limit)
            .fetch();
    }

    private BlogContent getPublicContentCursor(
        BlogContentType type,
        String seriesSlug,
        String hashtagSlug,
        Long cursor
    ) {
        if (cursor == null) {
            return null;
        }
        BlogContent cursorEntity = queryFactory
            .selectFrom(blogContent)
            .where(
                blogContent.id.eq(cursor),
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull(),
                type == null ? null : blogContent.contentType.eq(type),
                seriesFilter(type, seriesSlug),
                hashtagFilter(hashtagSlug)
            )
            .fetchOne();
        if (cursorEntity == null || cursorEntity.getPublishedAt() == null) {
            throw new BlogDomainException(BlogErrorCode.INVALID_CURSOR);
        }
        return cursorEntity;
    }

    private BlogContent getPublicHashtagContentCursor(Long hashtagId, BlogContentType type, Long cursor) {
        if (cursor == null) {
            return null;
        }
        BlogContent cursorEntity = queryFactory
            .select(blogContent)
            .from(blogContentHashtag)
            .join(blogContent).on(blogContent.id.eq(blogContentHashtag.contentId))
            .where(
                blogContentHashtag.hashtagId.eq(hashtagId),
                blogContent.id.eq(cursor),
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull(),
                type == null ? null : blogContent.contentType.eq(type)
            )
            .fetchOne();
        if (cursorEntity == null || cursorEntity.getPublishedAt() == null) {
            throw new BlogDomainException(BlogErrorCode.INVALID_CURSOR);
        }
        return cursorEntity;
    }

    private BlogSeriesContent getSeriesContentCursor(Long seriesId, Long cursor) {
        if (cursor == null) {
            return null;
        }
        BlogSeriesContent cursorRelation = queryFactory
            .selectFrom(blogSeriesContent)
            .join(blogContent).on(blogContent.id.eq(blogSeriesContent.contentId))
            .where(
                blogSeriesContent.seriesId.eq(seriesId),
                blogSeriesContent.contentId.eq(cursor),
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull()
            )
            .fetchOne();
        if (cursorRelation == null) {
            throw new BlogDomainException(BlogErrorCode.INVALID_CURSOR);
        }
        return cursorRelation;
    }

    private BooleanExpression seriesFilter(BlogContentType type, String seriesSlug) {
        if (seriesSlug == null || seriesSlug.isBlank()) {
            return null;
        }
        return JPAExpressions
            .selectOne()
            .from(blogSeriesContent, blogSeries)
            .where(
                blogSeriesContent.contentId.eq(blogContent.id),
                blogSeries.id.eq(blogSeriesContent.seriesId),
                blogSeries.slug.eq(seriesSlug.trim()),
                blogSeries.deletedAt.isNull(),
                type == null ? blogSeries.contentType.eq(blogContent.contentType) : blogSeries.contentType.eq(type)
            )
            .exists();
    }

    private BooleanExpression hashtagFilter(String hashtagSlug) {
        if (hashtagSlug == null || hashtagSlug.isBlank()) {
            return null;
        }
        return JPAExpressions
            .selectOne()
            .from(blogContentHashtag, blogHashtag)
            .where(
                blogContentHashtag.contentId.eq(blogContent.id),
                blogHashtag.id.eq(blogContentHashtag.hashtagId),
                blogHashtag.slug.eq(hashtagSlug.trim())
            )
            .exists();
    }

    private BooleanExpression cursorCondition(BlogContent cursorEntity, BlogContentSort sort) {
        if (cursorEntity == null) {
            return null;
        }
        return sort.isAscending()
            ? blogContent.publishedAt.gt(cursorEntity.getPublishedAt())
                .or(blogContent.publishedAt.eq(cursorEntity.getPublishedAt())
                    .and(blogContent.id.gt(cursorEntity.getId())))
            : blogContent.publishedAt.lt(cursorEntity.getPublishedAt())
                .or(blogContent.publishedAt.eq(cursorEntity.getPublishedAt())
                    .and(blogContent.id.lt(cursorEntity.getId())));
    }

    private BooleanExpression seriesContentCursorCondition(BlogSeriesContent cursorRelation) {
        if (cursorRelation == null) {
            return null;
        }
        return blogSeriesContent.displayOrder.gt(cursorRelation.getDisplayOrder())
            .or(blogSeriesContent.displayOrder.eq(cursorRelation.getDisplayOrder())
                .and(blogSeriesContent.contentId.gt(cursorRelation.getContentId())));
    }

    private OrderSpecifier<?>[] orderSpecifiers(BlogContentSort sort) {
        if (sort.isAscending()) {
            return new OrderSpecifier<?>[] {blogContent.publishedAt.asc(), blogContent.id.asc()};
        }
        return new OrderSpecifier<?>[] {blogContent.publishedAt.desc(), blogContent.id.desc()};
    }
}
