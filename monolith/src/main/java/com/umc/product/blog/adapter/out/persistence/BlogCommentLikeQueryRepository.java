package com.umc.product.blog.adapter.out.persistence;

import static com.umc.product.blog.domain.QBlogCommentLike.blogCommentLike;

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
public class BlogCommentLikeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Map<Long, Integer> countByCommentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Tuple> rows = queryFactory
            .select(blogCommentLike.commentId, blogCommentLike.count())
            .from(blogCommentLike)
            .where(blogCommentLike.commentId.in(commentIds))
            .groupBy(blogCommentLike.commentId)
            .fetch();

        Map<Long, Integer> result = new HashMap<>();
        for (Tuple row : rows) {
            Long commentId = row.get(blogCommentLike.commentId);
            Long count = row.get(blogCommentLike.count());
            result.put(commentId, count == null ? 0 : count.intValue());
        }
        return result;
    }

    public Set<Long> findLikedCommentIds(List<Long> commentIds, Long memberId) {
        if (commentIds == null || commentIds.isEmpty() || memberId == null) {
            return Set.of();
        }

        return new HashSet<>(queryFactory
            .select(blogCommentLike.commentId)
            .from(blogCommentLike)
            .where(
                blogCommentLike.commentId.in(commentIds),
                blogCommentLike.memberId.eq(memberId)
            )
            .fetch());
    }
}
