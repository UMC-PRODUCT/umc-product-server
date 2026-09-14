package com.umc.product.blog.adapter.out.persistence;

import static com.umc.product.blog.domain.QBlogComment.blogComment;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.blog.domain.BlogComment;
import com.umc.product.blog.domain.BlogCommentDeletionType;
import com.umc.product.blog.domain.BlogCommentSort;
import com.umc.product.blog.domain.BlogDomainException;
import com.umc.product.blog.domain.BlogErrorCode;
import com.umc.product.blog.domain.QBlogComment;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlogCommentQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final BlogCommentJpaRepository commentJpaRepository;

    public List<BlogComment> listTopLevel(
        Long contentId,
        BlogCommentSort sort,
        Long cursor,
        int limit
    ) {
        BlogComment cursorEntity = getCursorEntity(contentId, cursor);

        return queryFactory
            .selectFrom(blogComment)
            .where(
                blogComment.contentId.eq(contentId),
                blogComment.parentCommentId.isNull(),
                visibleTopLevelCondition(),
                cursorCondition(cursorEntity, sort)
            )
            .orderBy(orderSpecifiers(sort))
            .limit(limit)
            .fetch();
    }

    private BlogComment getCursorEntity(Long contentId, Long cursor) {
        if (cursor == null) {
            return null;
        }
        return commentJpaRepository.findByIdAndContentIdAndParentCommentIdIsNull(cursor, contentId)
            .orElseThrow(() -> new BlogDomainException(BlogErrorCode.INVALID_COMMENT_CURSOR));
    }

    public List<BlogComment> listRepliesByParentIds(List<Long> parentCommentIds) {
        if (parentCommentIds == null || parentCommentIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(blogComment)
            .where(
                blogComment.parentCommentId.in(parentCommentIds),
                blogComment.deletionType.eq(BlogCommentDeletionType.NONE)
            )
            .orderBy(blogComment.createdAt.asc(), blogComment.id.asc())
            .fetch();
    }

    public boolean existsVisibleReply(Long parentCommentId) {
        return queryFactory
            .selectOne()
            .from(blogComment)
            .where(
                blogComment.parentCommentId.eq(parentCommentId),
                blogComment.deletionType.eq(BlogCommentDeletionType.NONE)
            )
            .fetchFirst() != null;
    }

    private BooleanExpression visibleTopLevelCondition() {
        QBlogComment reply = new QBlogComment("reply");
        return blogComment.deletionType.eq(BlogCommentDeletionType.NONE)
            .or(JPAExpressions
                .selectOne()
                .from(reply)
                .where(
                    reply.parentCommentId.eq(blogComment.id),
                    reply.deletionType.eq(BlogCommentDeletionType.NONE)
                )
                .exists());
    }

    private BooleanExpression cursorCondition(BlogComment cursorEntity, BlogCommentSort sort) {
        if (cursorEntity == null) {
            return null;
        }
        return sort.isAscending()
            ? blogComment.createdAt.gt(cursorEntity.getCreatedAt())
                .or(blogComment.createdAt.eq(cursorEntity.getCreatedAt())
                    .and(blogComment.id.gt(cursorEntity.getId())))
            : blogComment.createdAt.lt(cursorEntity.getCreatedAt())
                .or(blogComment.createdAt.eq(cursorEntity.getCreatedAt())
                    .and(blogComment.id.lt(cursorEntity.getId())));
    }

    private OrderSpecifier<?>[] orderSpecifiers(BlogCommentSort sort) {
        if (sort.isAscending()) {
            return new OrderSpecifier<?>[] {blogComment.createdAt.asc(), blogComment.id.asc()};
        }
        return new OrderSpecifier<?>[] {blogComment.createdAt.desc(), blogComment.id.desc()};
    }
}
