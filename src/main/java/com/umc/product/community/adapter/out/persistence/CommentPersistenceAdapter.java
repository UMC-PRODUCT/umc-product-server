package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.application.port.in.post.ToggleCommentLikeUseCase.LikeResult;
import com.umc.product.community.application.port.out.LoadCommentPort;
import com.umc.product.community.application.port.out.SaveCommentPort;
import com.umc.product.community.domain.Comment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements LoadCommentPort, SaveCommentPort {

    private final CommentRepository commentRepository;

    @Override
    public Optional<Comment> findById(Long commentId) {
        return commentRepository.findById(commentId)
                .map(CommentJpaEntity::toDomain);
    }

    @Override
    public Page<Comment> findByPostId(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable)
                .map(CommentJpaEntity::toDomain);
    }

    @Override
    public int countByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    @Override
    public Map<Long, Integer> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Object[]> results = commentRepository.countByPostIdIn(postIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],           // postId
                        row -> ((Number) row[1]).intValue()  // count
                ));
    }

    @Override
    public Comment save(Comment comment) {
        CommentJpaEntity entity = CommentJpaEntity.from(comment);
        CommentJpaEntity saved = commentRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public void delete(Comment comment) {
        if (comment.getCommentId() != null) {
            commentRepository.deleteById(comment.getCommentId().id());
        }
    }

    @Override
    public LikeResult toggleLike(Long commentId, Long challengerId) {
        CommentJpaEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        boolean liked = entity.toggleLike(challengerId);
        return new LikeResult(liked, entity.getLikeCount());
    }
}
