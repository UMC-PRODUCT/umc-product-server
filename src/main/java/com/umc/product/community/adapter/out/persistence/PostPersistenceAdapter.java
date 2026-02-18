package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.application.port.in.command.post.TogglePostLikeUseCase.LikeResult;
import com.umc.product.community.application.port.in.query.dto.PostSearchQuery;
import com.umc.product.community.application.port.out.dto.PostSearchData;
import com.umc.product.community.application.port.out.dto.PostWithAuthor;
import com.umc.product.community.application.port.out.post.LoadPostPort;
import com.umc.product.community.application.port.out.post.SavePostPort;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements LoadPostPort, SavePostPort {

    private final PostRepository postRepository;
    private final PostQueryRepository postQueryRepository;
    // private final PostLikeRepository postLikeRepository;

    @Override
    public Post save(Post post) {
        // UPDATE용 (authorChallengerId 필요 없음)
        if (post.getPostId() != null) {
            PostJpaEntity entity = postRepository.findById(post.getPostId().id())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
            entity.update(post.getTitle(), post.getContent(), post.getCategory());
            return entity.toDomain();
        }

        throw new IllegalArgumentException("새 게시글 생성 시에는 save(Post post, Long authorChallengerId)를 사용하세요.");
    }

    @Override
    public Post save(Post post, Long authorChallengerId) {
        // CREATE용 (authorChallengerId 포함)
        if (post.getPostId() != null) {
            throw new IllegalArgumentException("이미 ID가 있는 게시글은 save(Post post)를 사용하세요.");
        }

        PostJpaEntity entity = PostJpaEntity.from(post, authorChallengerId);
        PostJpaEntity saved = postRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public void delete(Post post) {
        if (post.getPostId() != null) {
            postRepository.deleteById(post.getPostId().id());
        }
    }

    @Override
    public void deleteById(Long postId) {
        postRepository.deleteById(postId);
    }

    @Override
    public Page<Post> findAllByQuery(PostSearchQuery query, Pageable pageable) {
        return postQueryRepository.findAllByQuery(query, pageable);
    }

    @Override
    public Optional<Post> findById(Long postId) {
        return postRepository.findById(postId)
            .map(PostJpaEntity::toDomain);
    }

    @Override
    public Optional<PostWithAuthor> findByIdWithAuthor(Long postId) {
        return postRepository.findById(postId)
            .map(entity -> new PostWithAuthor(entity.toDomain(), entity.getAuthorChallengerId()));
    }

    @Override
    public Optional<PostWithAuthor> findByIdWithAuthor(Long postId, Long viewerChallengerId) {
        return postRepository.findById(postId)
            .map(entity -> new PostWithAuthor(entity.toDomain(viewerChallengerId), entity.getAuthorChallengerId()));
    }

    @Override
    public List<Post> findByCategory(Category category) {
        return postRepository.findByCategory(category).stream()
            .map(PostJpaEntity::toDomain)
            .toList();
    }

    @Override
    public LikeResult toggleLike(Long postId, Long challengerId) {
        PostJpaEntity entity = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        boolean liked = entity.toggleLike(challengerId);
        return new LikeResult(liked, entity.getLikeCount());
    }

    @Override
    public Page<PostSearchData> searchByKeyword(String keyword, Pageable pageable) {
        return postQueryRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public Long findAuthorIdByPostId(Long postId) {
        return postRepository.findById(postId)
            .map(PostJpaEntity::getAuthorChallengerId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    @Override
    public Map<Long, Long> findAuthorIdsByPostIds(List<Long> postIds) {
        return postRepository.findAuthorIdsMapByPostIds(postIds);
    }

    @Override
    public Page<Post> findByAuthorChallengerId(Long challengerId, Pageable pageable) {
        return postQueryRepository.findByAuthorChallengerId(challengerId, pageable);
    }

    @Override
    public Page<Post> findCommentedPostsByChallengerId(Long challengerId, Pageable pageable) {
        return postQueryRepository.findCommentedPostsByChallengerId(challengerId, pageable);
    }

    @Override
    public Page<Post> findScrappedPostsByChallengerId(Long challengerId, Pageable pageable) {
        return postQueryRepository.findScrappedPostsByChallengerId(challengerId, pageable);
    }
}
