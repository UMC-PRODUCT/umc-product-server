package com.umc.product.test.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.community.application.port.in.command.comment.CreateCommentUseCase;
import com.umc.product.community.application.port.in.command.post.CreatePostUseCase;
import com.umc.product.community.application.port.in.command.trophy.CreateTrophyUseCase;
import com.umc.product.community.application.port.in.query.dto.CommentInfo;
import com.umc.product.community.application.port.in.query.dto.PostInfo;
import com.umc.product.community.application.port.in.query.dto.TrophyInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.test.application.port.in.command.SeedCommunityUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedCommunityCommand;
import com.umc.product.test.application.port.in.command.dto.SeedCommunityResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Community(게시글·댓글·트로피) 시딩 서비스. ADR-017 참조.
 * <p>
 * 활성 기수(또는 지정 기수)의 챌린저 풀에서 무작위 작성자를 선택해 Post → Comment → Trophy
 * 순서로 시딩한다. Hexagonal 원칙을 따라 다른 도메인의 UseCase 만 호출하며, 각 create
 * 호출은 자체 트랜잭션 경계를 가져 한 건 실패가 다른 건 시딩을 막지 않는다.
 * <p>
 * <b>트랜잭션 정책</b>: {@code seed()} 메서드는 {@link Propagation#NOT_SUPPORTED} 로 외부
 * 트랜잭션을 차단해 각 create 호출이 독립 트랜잭션으로 커밋되도록 한다(실패 격리).
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class CommunitySeedService implements SeedCommunityUseCase {

    private final DummyCommunityFactory dummyCommunityFactory;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final CreatePostUseCase createPostUseCase;
    private final CreateCommentUseCase createCommentUseCase;
    private final CreateTrophyUseCase createTrophyUseCase;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SeedCommunityResult seed(SeedCommunityCommand command) {
        Long gisuId = command.gisuId() != null ? command.gisuId() : getGisuUseCase.getActiveGisuId();
        List<Long> challengerIds = resolveChallengerPool(gisuId);

        if (challengerIds.isEmpty()) {
            String reason = "no challengers in gisu %d — call /test/seed/challengers first".formatted(gisuId);
            log.info("community seed skipped: {}", reason);
            return SeedCommunityResult.skipped(gisuId, reason);
        }

        long startedAt = System.currentTimeMillis();
        log.info(
            "community seed start: gisuId={}, challengerPool={}, postCount={}, commentsPerPost={}, trophyCount={}",
            gisuId, challengerIds.size(), command.postCount(), command.commentsPerPost(), command.trophyCount()
        );

        SeedPosts posts = seedPosts(challengerIds, command.postCount());
        SeedCounts comments = seedComments(posts.createdPostIds(), challengerIds, command.commentsPerPost());
        SeedTrophies trophies = seedTrophies(challengerIds, command.trophyCount());

        long elapsedMs = System.currentTimeMillis() - startedAt;
        log.info(
            "community seed completed in {}ms: posts={}/{} (failed={}), comments={} (failed={}), trophies={}/{} (failed={})",
            elapsedMs,
            posts.createdPostIds().size(), command.postCount(), posts.failed(),
            comments.created(), comments.failed(),
            trophies.createdTrophyIds().size(), command.trophyCount(), trophies.failed()
        );

        return new SeedCommunityResult(
            gisuId,
            posts.createdPostIds(),
            comments.createdCommentIds(),
            trophies.createdTrophyIds(),
            posts.failed(),
            comments.failed(),
            trophies.failed(),
            false,
            null
        );
    }

    private List<Long> resolveChallengerPool(Long gisuId) {
        List<ChallengerInfo> challengers = getChallengerUseCase.getAllByGisuId(gisuId);
        return challengers.stream().map(ChallengerInfo::challengerId).toList();
    }

    private SeedPosts seedPosts(List<Long> challengerIds, int target) {
        if (target <= 0) {
            return new SeedPosts(List.of(), 0);
        }
        List<Long> createdPostIds = new ArrayList<>(target);
        int failed = 0;
        for (int i = 0; i < target; i++) {
            Long authorChallengerId = pickRandom(challengerIds);
            try {
                PostInfo post = createPostUseCase.createPost(dummyCommunityFactory.nextPostCommand(authorChallengerId));
                createdPostIds.add(post.postId());
            } catch (Exception e) {
                failed++;
                log.error("community seed post create failed (challengerId={}): {}", authorChallengerId, e.toString());
            }
        }
        return new SeedPosts(createdPostIds, failed);
    }

    private SeedCounts seedComments(List<Long> postIds, List<Long> challengerIds, int commentsPerPost) {
        if (commentsPerPost <= 0 || postIds.isEmpty()) {
            return new SeedCounts(List.of(), 0, 0);
        }
        List<Long> createdCommentIds = new ArrayList<>(postIds.size() * commentsPerPost);
        int created = 0;
        int failed = 0;
        for (Long postId : postIds) {
            for (int i = 0; i < commentsPerPost; i++) {
                Long authorChallengerId = pickRandom(challengerIds);
                try {
                    CommentInfo comment = createCommentUseCase.create(
                        dummyCommunityFactory.nextCommentCommand(postId, authorChallengerId)
                    );
                    createdCommentIds.add(comment.commentId());
                    created++;
                } catch (Exception e) {
                    failed++;
                    log.error(
                        "community seed comment create failed (postId={}, challengerId={}): {}",
                        postId, authorChallengerId, e.toString()
                    );
                }
            }
        }
        return new SeedCounts(createdCommentIds, created, failed);
    }

    private SeedTrophies seedTrophies(List<Long> challengerIds, int target) {
        if (target <= 0) {
            return new SeedTrophies(List.of(), 0);
        }
        List<Long> createdTrophyIds = new ArrayList<>(target);
        int failed = 0;
        for (int i = 0; i < target; i++) {
            Long challengerId = pickRandom(challengerIds);
            try {
                TrophyInfo trophy = createTrophyUseCase.createTrophy(
                    dummyCommunityFactory.nextTrophyCommand(challengerId)
                );
                createdTrophyIds.add(trophy.trophyId());
            } catch (Exception e) {
                failed++;
                log.error("community seed trophy create failed (challengerId={}): {}", challengerId, e.toString());
            }
        }
        return new SeedTrophies(createdTrophyIds, failed);
    }

    private Long pickRandom(List<Long> ids) {
        return ids.get(ThreadLocalRandom.current().nextInt(ids.size()));
    }

    private record SeedPosts(List<Long> createdPostIds, int failed) {
    }

    private record SeedCounts(List<Long> createdCommentIds, int created, int failed) {
    }

    private record SeedTrophies(List<Long> createdTrophyIds, int failed) {
    }
}
