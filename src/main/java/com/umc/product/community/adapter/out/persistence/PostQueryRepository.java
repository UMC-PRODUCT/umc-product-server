package com.umc.product.community.adapter.out.persistence;


import static com.umc.product.community.adapter.out.persistence.entity.QCommentJpaEntity.commentJpaEntity;
import static com.umc.product.community.adapter.out.persistence.entity.QPostJpaEntity.postJpaEntity;
import static com.umc.product.community.adapter.out.persistence.entity.QScrapJpaEntity.scrapJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.community.adapter.out.persistence.entity.PostJpaEntity;
import com.umc.product.community.application.port.in.query.dto.PostSearchQuery;
import com.umc.product.community.application.port.in.query.dto.PostSearchResult.MatchType;
import com.umc.product.community.application.port.out.dto.PostSearchData;
import com.umc.product.community.domain.Post;
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

        List<PostJpaEntity> results = queryFactory
            .selectFrom(postJpaEntity)
            .where(condition)
            .orderBy(postJpaEntity.createdAt.desc())
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
        if (query.category() != null) {
            return postJpaEntity.category.eq(query.category());
        }
        return null;
    }

    public Page<Post> searchByKeyword(String keyword, Pageable pageable) {
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

        List<Post> postInfos = results.stream()
            .map(PostJpaEntity::toDomain)
            .toList();

        return new PageImpl<>(postInfos, pageable, totalCount != null ? totalCount : 0);

        // TODO: 여기서 PostSearchData 반환하는 중
//        List<PostSearchData> searchDataList = results.stream()
//            .map(entity -> toSearchData(entity, searchKeyword))
//            .toList();
//
//        return new PageImpl<>(searchDataList, pageable, totalCount != null ? totalCount : 0);
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

        return PostSearchData.from(entity, matchType, score);
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

    /**
     * 챌린저가 작성한 게시글 목록 조회
     */
    public Page<Post> findByAuthorChallengerId(Long challengerId, Pageable pageable) {
        List<PostJpaEntity> results = queryFactory
            .selectFrom(postJpaEntity)
            .where(postJpaEntity.authorChallengerId.eq(challengerId))
            .orderBy(postJpaEntity.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long totalCount = queryFactory
            .select(postJpaEntity.count())
            .from(postJpaEntity)
            .where(postJpaEntity.authorChallengerId.eq(challengerId))
            .fetchOne();

        List<Post> posts = results.stream()
            .map(PostJpaEntity::toDomain)
            .toList();

        return new PageImpl<>(posts, pageable, totalCount != null ? totalCount : 0);
    }

    /**
     * 챌린저가 댓글을 단 게시글 목록 조회 (중복 제거, 최신 댓글 순)
     */
    public Page<Post> findCommentedPostsByChallengerId(Long challengerId, Pageable pageable) {
        // 1. JOIN과 GROUP BY를 사용하여 단일 쿼리로 조회 (DB에서 정렬)
        List<PostJpaEntity> results = queryFactory
            .selectFrom(postJpaEntity)
            .innerJoin(commentJpaEntity)
            .on(postJpaEntity.id.eq(commentJpaEntity.postId))
            .where(commentJpaEntity.challengerId.eq(challengerId))
            .groupBy(postJpaEntity.id)
            .orderBy(commentJpaEntity.createdAt.max().desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (results.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. 전체 개수 조회
        Long totalCount = queryFactory
            .select(commentJpaEntity.postId.countDistinct())
            .from(commentJpaEntity)
            .where(commentJpaEntity.challengerId.eq(challengerId))
            .fetchOne();

        List<Post> posts = results.stream()
            .map(PostJpaEntity::toDomain)
            .toList();

        return new PageImpl<>(posts, pageable, totalCount != null ? totalCount : 0);
    }

    /**
     * 챌린저가 스크랩한 게시글 목록 조회 (최신 스크랩 순)
     */
    public Page<Post> findScrappedPostsByChallengerId(Long challengerId, Pageable pageable) {
        // 1. JOIN을 사용하여 단일 쿼리로 조회 (DB에서 정렬)
        List<PostJpaEntity> results = queryFactory
            .selectFrom(postJpaEntity)
            .innerJoin(scrapJpaEntity)
            .on(postJpaEntity.id.eq(scrapJpaEntity.postId))
            .where(scrapJpaEntity.challengerId.eq(challengerId))
            .orderBy(scrapJpaEntity.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (results.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. 전체 개수 조회
        Long totalCount = queryFactory
            .select(scrapJpaEntity.count())
            .from(scrapJpaEntity)
            .where(scrapJpaEntity.challengerId.eq(challengerId))
            .fetchOne();

        List<Post> posts = results.stream()
            .map(PostJpaEntity::toDomain)
            .toList();

        return new PageImpl<>(posts, pageable, totalCount != null ? totalCount : 0);
    }
}
