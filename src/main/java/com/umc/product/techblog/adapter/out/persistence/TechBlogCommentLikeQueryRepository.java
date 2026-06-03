package com.umc.product.techblog.adapter.out.persistence;

import static com.umc.product.techblog.domain.QTechBlogCommentLike.techBlogCommentLike;

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
            .select(techBlogCommentLike.commentId, techBlogCommentLike.count())
            .from(techBlogCommentLike)
            .where(techBlogCommentLike.commentId.in(commentIds))
            .groupBy(techBlogCommentLike.commentId)
            .fetch();

        Map<Long, Integer> result = new HashMap<>();
        for (Tuple row : rows) {
            Long commentId = row.get(techBlogCommentLike.commentId);
            Long count = row.get(techBlogCommentLike.count());
            result.put(commentId, count == null ? 0 : count.intValue());
        }
        return result;
    }

    public Set<Long> findLikedCommentIds(List<Long> commentIds, Long memberId) {
        if (commentIds == null || commentIds.isEmpty() || memberId == null) {
            return Set.of();
        }

        return new HashSet<>(queryFactory
            .select(techBlogCommentLike.commentId)
            .from(techBlogCommentLike)
            .where(
                techBlogCommentLike.commentId.in(commentIds),
                techBlogCommentLike.memberId.eq(memberId)
            )
            .fetch());
    }
}
