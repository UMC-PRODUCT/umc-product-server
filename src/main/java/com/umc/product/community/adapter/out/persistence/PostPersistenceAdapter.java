package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.application.port.in.post.TogglePostLikeUseCase.LikeResult;
import com.umc.product.community.application.port.in.post.query.PostSearchQuery;
import com.umc.product.community.application.port.out.LoadPostPort;
import com.umc.product.community.application.port.out.SavePostPort;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements LoadPostPort, SavePostPort {

    private final PostRepository postRepository;
    // private final PostLikeRepository postLikeRepository;

    @Override
    public Post save(Post post) {
        // ID가 있으면 UPDATE, 없으면 INSERT
        if (post.getPostId() != null) {
            PostJpaEntity entity = postRepository.findById(post.getPostId().id())
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
            entity.update(post.getTitle(), post.getContent(), post.getCategory(), post.getRegion());
            return entity.toDomain();
        }

        PostJpaEntity entity = PostJpaEntity.from(post);
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
    public List<Post> findAllByQuery(PostSearchQuery query) {
        // TODO: QueryDSL로 동적 쿼리 구현 필요
        return postRepository.findAll().stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Post> findById(Long postId) {
        return postRepository.findById(postId)
                .map(PostJpaEntity::toDomain);
    }

    @Override
    public List<Post> findByCategory(Category category) {
        return postRepository.findByCategory(category).stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Post> findByRegion(String region) {
        return postRepository.findByRegion(region).stream()
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
}
