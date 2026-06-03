package com.umc.product.techblog.adapter.out.persistence;

import static com.umc.product.techblog.domain.QTechBlogComment.techBlogComment;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.techblog.domain.QTechBlogComment;
import com.umc.product.techblog.domain.TechBlogComment;
import com.umc.product.techblog.domain.TechBlogCommentDeletionType;
import com.umc.product.techblog.domain.TechBlogCommentSort;
import com.umc.product.techblog.domain.TechBlogDomainException;
import com.umc.product.techblog.domain.TechBlogErrorCode;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TechBlogCommentQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final TechBlogCommentJpaRepository commentJpaRepository;

    public List<TechBlogComment> listTopLevel(
        Long contentId,
        TechBlogCommentSort sort,
        Long cursor,
        int limit
    ) {
        TechBlogComment cursorEntity = getCursorEntity(contentId, cursor);

        return queryFactory
            .selectFrom(techBlogComment)
            .where(
                techBlogComment.contentId.eq(contentId),
                techBlogComment.parentCommentId.isNull(),
                visibleTopLevelCondition(),
                cursorCondition(cursorEntity, sort)
            )
            .orderBy(orderSpecifiers(sort))
            .limit(limit)
            .fetch();
    }

    private TechBlogComment getCursorEntity(Long contentId, Long cursor) {
        if (cursor == null) {
            return null;
        }
        return commentJpaRepository.findByIdAndContentIdAndParentCommentIdIsNull(cursor, contentId)
            .orElseThrow(() -> new TechBlogDomainException(TechBlogErrorCode.INVALID_COMMENT_CURSOR));
    }

    public List<TechBlogComment> listRepliesByParentIds(List<Long> parentCommentIds) {
        if (parentCommentIds == null || parentCommentIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(techBlogComment)
            .where(
                techBlogComment.parentCommentId.in(parentCommentIds),
                techBlogComment.deletionType.eq(TechBlogCommentDeletionType.NONE)
            )
            .orderBy(techBlogComment.createdAt.asc(), techBlogComment.id.asc())
            .fetch();
    }

    public boolean existsVisibleReply(Long parentCommentId) {
        return queryFactory
            .selectOne()
            .from(techBlogComment)
            .where(
                techBlogComment.parentCommentId.eq(parentCommentId),
                techBlogComment.deletionType.eq(TechBlogCommentDeletionType.NONE)
            )
            .fetchFirst() != null;
    }

    private BooleanExpression visibleTopLevelCondition() {
        QTechBlogComment reply = new QTechBlogComment("reply");
        return techBlogComment.deletionType.eq(TechBlogCommentDeletionType.NONE)
            .or(JPAExpressions
                .selectOne()
                .from(reply)
                .where(
                    reply.parentCommentId.eq(techBlogComment.id),
                    reply.deletionType.eq(TechBlogCommentDeletionType.NONE)
                )
                .exists());
    }

    private BooleanExpression cursorCondition(TechBlogComment cursorEntity, TechBlogCommentSort sort) {
        if (cursorEntity == null) {
            return null;
        }
        return sort.isAscending()
            ? techBlogComment.createdAt.gt(cursorEntity.getCreatedAt())
                .or(techBlogComment.createdAt.eq(cursorEntity.getCreatedAt())
                    .and(techBlogComment.id.gt(cursorEntity.getId())))
            : techBlogComment.createdAt.lt(cursorEntity.getCreatedAt())
                .or(techBlogComment.createdAt.eq(cursorEntity.getCreatedAt())
                    .and(techBlogComment.id.lt(cursorEntity.getId())));
    }

    private OrderSpecifier<?>[] orderSpecifiers(TechBlogCommentSort sort) {
        if (sort.isAscending()) {
            return new OrderSpecifier<?>[] {techBlogComment.createdAt.asc(), techBlogComment.id.asc()};
        }
        return new OrderSpecifier<?>[] {techBlogComment.createdAt.desc(), techBlogComment.id.desc()};
    }
}
