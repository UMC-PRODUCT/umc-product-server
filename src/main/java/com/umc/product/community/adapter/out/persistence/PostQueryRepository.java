package com.umc.product.community.adapter.out.persistence;

import static com.umc.product.community.adapter.out.persistence.QPostJpaEntity.postJpaEntity;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.community.application.port.in.post.query.PostSearchQuery;
import com.umc.product.community.application.port.in.post.query.PostSearchResult.MatchType;
import com.umc.product.community.application.port.out.PostSearchData;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import com.umc.product.community.domain.enums.PostSortType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Post> findAllByQuery(PostSearchQuery query, Pageable pageable) {
        BooleanExpression condition = buildCondition(query);
        OrderSpecifier<?> orderSpecifier = buildOrderSpecifier(query.sort());

        List<PostJpaEntity> results = queryFactory
                .selectFrom(postJpaEntity)
                .where(condition)
                .orderBy(orderSpecifier, postJpaEntity.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(postJpaEntity.count())
                .from(postJpaEntity)
                .where(condition)
                .fetchOne();

        List<Post> posts = results.stream()
                .map(PostJpaEntity::toDomain)
                .toList();

        return new PageImpl<>(posts, pageable, totalCount != null ? totalCount : 0);
    }

    private BooleanExpression buildCondition(PostSearchQuery query) {
        // 조건 없음 (전체 조회)
        return null;
    }

    private OrderSpecifier<?> buildOrderSpecifier(PostSortType sort) {
        if (sort == null || sort == PostSortType.ALL) {
            return postJpaEntity.createdAt.desc();
        }

        return switch (sort) {
            case SOFT -> postJpaEntity.likedChallengerIds.size().desc();
            case HARD -> postJpaEntity.likedChallengerIds.size().asc();
            default -> postJpaEntity.createdAt.desc();
        };
    }

    public Page<PostSearchData> searchByKeyword(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return Page.empty(pageable);
        }

        String searchKeyword = keyword.trim().toLowerCase();

        BooleanExpression searchCondition = titleContains(searchKeyword)
                .or(contentContains(searchKeyword));

        NumberExpression<Integer> relevanceScore = createRelevanceScore(searchKeyword);

        List<PostJpaEntity> results = queryFactory
                .selectFrom(postJpaEntity)
                .where(searchCondition)
                .orderBy(
                        relevanceScore.desc(),
                        postJpaEntity.createdAt.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(postJpaEntity.count())
                .from(postJpaEntity)
                .where(searchCondition)
                .fetchOne();

        List<PostSearchData> searchDataList = results.stream()
                .map(entity -> toSearchData(entity, searchKeyword))
                .toList();

        return new PageImpl<>(searchDataList, pageable, totalCount != null ? totalCount : 0);
    }

    private BooleanExpression titleContains(String keyword) {
        return postJpaEntity.title.lower().contains(keyword);
    }

    private BooleanExpression contentContains(String keyword) {
        return postJpaEntity.content.lower().contains(keyword);
    }

    private NumberExpression<Integer> createRelevanceScore(String keyword) {
        return new CaseBuilder()
                .when(postJpaEntity.title.lower().startsWith(keyword))
                .then(100)
                .when(postJpaEntity.title.lower().contains(keyword))
                .then(50)
                .when(postJpaEntity.content.lower().contains(keyword))
                .then(10)
                .otherwise(0);
    }

    private PostSearchData toSearchData(PostJpaEntity entity, String keyword) {
        MatchType matchType = determineMatchType(entity, keyword);
        int score = calculateScore(entity, keyword);

        return new PostSearchData(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getCategory(),
                entity.getRegion(),
                entity.isAnonymous(),
                entity.getLikeCount(),
                entity.getCreatedAt(),
                matchType,
                score
        );
    }

    private MatchType determineMatchType(PostJpaEntity entity, String keyword) {
        String titleLower = entity.getTitle().toLowerCase();

        if (titleLower.startsWith(keyword)) {
            return MatchType.TITLE_START;
        }
        if (titleLower.contains(keyword)) {
            return MatchType.TITLE_CONTAIN;
        }
        return MatchType.CONTENT;
    }

    private int calculateScore(PostJpaEntity entity, String keyword) {
        String titleLower = entity.getTitle().toLowerCase();

        if (titleLower.startsWith(keyword)) {
            return 100;
        }
        if (titleLower.contains(keyword)) {
            return 50;
        }
        return 10;
    }
}
