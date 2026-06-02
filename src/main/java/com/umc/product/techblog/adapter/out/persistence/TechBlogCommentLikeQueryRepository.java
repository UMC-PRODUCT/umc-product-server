package com.umc.product.techblog.adapter.out.persistence;

import static com.umc.product.techblog.adapter.out.persistence.entity.QTechBlogCommentLikeJpaEntity.techBlogCommentLikeJpaEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TechBlogCommentLikeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Map<Long, Integer> countByCommentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Tuple> rows = queryFactory
            .select(techBlogCommentLikeJpaEntity.commentId, techBlogCommentLikeJpaEntity.count())
            .from(techBlogCommentLikeJpaEntity)
            .where(techBlogCommentLikeJpaEntity.commentId.in(commentIds))
            .groupBy(techBlogCommentLikeJpaEntity.commentId)
            .fetch();

        Map<Long, Integer> result = new HashMap<>();
        for (Tuple row : rows) {
            Long commentId = row.get(techBlogCommentLikeJpaEntity.commentId);
            Long count = row.get(techBlogCommentLikeJpaEntity.count());
            result.put(commentId, count == null ? 0 : count.intValue());
        }
        return result;
    }

    public Set<Long> findLikedCommentIds(List<Long> commentIds, Long memberId) {
        if (commentIds == null || commentIds.isEmpty() || memberId == null) {
            return Set.of();
        }

        return new HashSet<>(queryFactory
            .select(techBlogCommentLikeJpaEntity.commentId)
            .from(techBlogCommentLikeJpaEntity)
            .where(
                techBlogCommentLikeJpaEntity.commentId.in(commentIds),
                techBlogCommentLikeJpaEntity.memberId.eq(memberId)
            )
            .fetch());
    }
}
