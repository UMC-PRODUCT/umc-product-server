package com.umc.product.blog.adapter.out.persistence;

import static com.umc.product.blog.domain.QBlogContent.blogContent;
import static com.umc.product.blog.domain.QBlogContentHashtag.blogContentHashtag;
import static com.umc.product.blog.domain.QBlogHashtag.blogHashtag;
import static com.umc.product.blog.domain.QBlogSeries.blogSeries;
import static com.umc.product.blog.domain.QBlogSeriesContent.blogSeriesContent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.blog.application.port.out.dto.BlogSeoPathRow;
import com.umc.product.blog.domain.BlogContent;
import com.umc.product.blog.domain.BlogContentStatus;
import com.umc.product.blog.domain.BlogHashtag;
import com.umc.product.blog.domain.BlogSeries;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlogSeoQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<BlogSeoPathRow> listPublicSeoPaths() {
        List<BlogSeoPathRow> rows = new ArrayList<>();
        rows.addAll(listContentPaths());
        rows.addAll(listSeriesPaths());
        rows.addAll(listHashtagPaths());
        return rows;
    }

    private List<BlogSeoPathRow> listContentPaths() {
        return queryFactory
            .selectFrom(blogContent)
            .where(
                blogContent.status.eq(BlogContentStatus.PUBLISHED),
                blogContent.deletedAt.isNull()
            )
            .fetch()
            .stream()
            .map(this::contentPath)
            .toList();
    }

    private List<BlogSeoPathRow> listSeriesPaths() {
        return queryFactory
            .selectFrom(blogSeries)
            .where(
                blogSeries.deletedAt.isNull(),
                JPAExpressions
                    .selectOne()
                    .from(blogSeriesContent, blogContent)
                    .where(
                        blogSeriesContent.seriesId.eq(blogSeries.id),
                        blogContent.id.eq(blogSeriesContent.contentId),
                        blogContent.status.eq(BlogContentStatus.PUBLISHED),
                        blogContent.deletedAt.isNull()
                    )
                    .exists()
            )
            .fetch()
            .stream()
            .map(this::seriesPath)
            .toList();
    }

    private List<BlogSeoPathRow> listHashtagPaths() {
        return queryFactory
            .selectFrom(blogHashtag)
            .where(
                JPAExpressions
                    .selectOne()
                    .from(blogContentHashtag, blogContent)
                    .where(
                        blogContentHashtag.hashtagId.eq(blogHashtag.id),
                        blogContent.id.eq(blogContentHashtag.contentId),
                        blogContent.status.eq(BlogContentStatus.PUBLISHED),
                        blogContent.deletedAt.isNull()
                    )
                    .exists()
            )
            .fetch()
            .stream()
            .map(this::hashtagPath)
            .toList();
    }

    private BlogSeoPathRow contentPath(BlogContent content) {
        return new BlogSeoPathRow(
            "content",
            "/" + content.getContentType().name().toLowerCase() + "/" + content.getSlug(),
            content.getUpdatedAt()
        );
    }

    private BlogSeoPathRow seriesPath(BlogSeries series) {
        return new BlogSeoPathRow(
            "series",
            "/series/" + series.getContentType().name().toLowerCase() + "/" + series.getSlug(),
            series.getUpdatedAt()
        );
    }

    private BlogSeoPathRow hashtagPath(BlogHashtag hashtag) {
        return new BlogSeoPathRow("hashtag", "/hashtags/" + hashtag.getSlug(), hashtag.getUpdatedAt());
    }
}
