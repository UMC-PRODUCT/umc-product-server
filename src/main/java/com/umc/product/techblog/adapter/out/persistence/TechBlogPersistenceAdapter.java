package com.umc.product.techblog.adapter.out.persistence;

import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogCommentJpaEntity;
import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogCommentLikeJpaEntity;
import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogContentJpaEntity;
import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogContentLikeJpaEntity;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogLikeInfo;
import com.umc.product.techblog.application.port.out.LoadTechBlogCommentPort;
import com.umc.product.techblog.application.port.out.LoadTechBlogContentPort;
import com.umc.product.techblog.application.port.out.SaveTechBlogCommentPort;
import com.umc.product.techblog.application.port.out.SaveTechBlogContentPort;
import com.umc.product.techblog.domain.TechBlogComment;
import com.umc.product.techblog.domain.TechBlogCommentDeletionType;
import com.umc.product.techblog.domain.TechBlogCommentSort;
import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentType;
import com.umc.product.techblog.domain.TechBlogDomainException;
import com.umc.product.techblog.domain.TechBlogErrorCode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechBlogPersistenceAdapter implements LoadTechBlogContentPort, SaveTechBlogContentPort,
    LoadTechBlogCommentPort, SaveTechBlogCommentPort {

    private final TechBlogContentJpaRepository contentJpaRepository;
    private final TechBlogContentLikeJpaRepository contentLikeJpaRepository;
    private final TechBlogCommentJpaRepository commentJpaRepository;
    private final TechBlogCommentLikeJpaRepository commentLikeJpaRepository;
    private final TechBlogCommentQueryRepository commentQueryRepository;

    @Override
    public Optional<TechBlogContent> findByTypeAndSlug(TechBlogContentType type, String slug) {
        return contentJpaRepository.findByContentTypeAndSlug(type, slug)
            .map(TechBlogContentJpaEntity::toDomain);
    }

    @Override
    public int countLikesByContentId(Long contentId) {
        return contentLikeJpaRepository.countByContentId(contentId);
    }

    @Override
    public boolean existsLikeByContentIdAndMemberId(Long contentId, Long memberId) {
        return memberId != null && contentLikeJpaRepository.existsByContentIdAndMemberId(contentId, memberId);
    }

    @Override
    public TechBlogContent save(TechBlogContent content) {
        return contentJpaRepository.save(TechBlogContentJpaEntity.from(content)).toDomain();
    }

    @Override
    public TechBlogLikeInfo toggleContentLike(Long contentId, Long memberId) {
        boolean liked;
        if (contentLikeJpaRepository.existsByContentIdAndMemberId(contentId, memberId)) {
            contentLikeJpaRepository.deleteByContentIdAndMemberId(contentId, memberId);
            liked = false;
        } else {
            contentLikeJpaRepository.save(new TechBlogContentLikeJpaEntity(contentId, memberId));
            liked = true;
        }
        return new TechBlogLikeInfo(liked, contentLikeJpaRepository.countByContentId(contentId));
    }

    @Override
    public Optional<TechBlogComment> findById(Long commentId) {
        return commentJpaRepository.findById(commentId)
            .map(TechBlogCommentJpaEntity::toDomain);
    }

    @Override
    public Optional<TechBlogComment> findByIdAndContentId(Long commentId, Long contentId) {
        return commentJpaRepository.findByIdAndContentId(commentId, contentId)
            .map(TechBlogCommentJpaEntity::toDomain);
    }

    @Override
    public List<TechBlogComment> listTopLevel(Long contentId, TechBlogCommentSort sort, Long cursor, int size) {
        return commentQueryRepository.listTopLevel(contentId, sort, cursor, size).stream()
            .map(TechBlogCommentJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<TechBlogComment> listRepliesByParentIds(List<Long> parentCommentIds) {
        if (parentCommentIds == null || parentCommentIds.isEmpty()) {
            return List.of();
        }
        return commentJpaRepository
            .findByParentCommentIdInAndDeletionTypeOrderByCreatedAtAscIdAsc(
                parentCommentIds,
                TechBlogCommentDeletionType.NONE
            )
            .stream()
            .map(TechBlogCommentJpaEntity::toDomain)
            .toList();
    }

    @Override
    public boolean existsVisibleReply(Long parentCommentId) {
        return commentJpaRepository.existsByParentCommentIdAndDeletionType(
            parentCommentId,
            TechBlogCommentDeletionType.NONE
        );
    }

    @Override
    public Map<Long, Integer> countLikesByCommentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return new HashMap<>();
        }
        return commentLikeJpaRepository.countByCommentIds(commentIds).stream()
            .collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> ((Number) row[1]).intValue()
            ));
    }

    @Override
    public Set<Long> findLikedCommentIds(List<Long> commentIds, Long memberId) {
        if (commentIds == null || commentIds.isEmpty() || memberId == null) {
            return Set.of();
        }
        return new HashSet<>(commentLikeJpaRepository.findLikedCommentIds(commentIds, memberId));
    }

    @Override
    public TechBlogComment save(TechBlogComment comment) {
        return commentJpaRepository.save(TechBlogCommentJpaEntity.from(comment)).toDomain();
    }

    @Override
    public TechBlogComment updateContent(Long commentId, String content) {
        TechBlogCommentJpaEntity entity = getCommentEntity(commentId);
        entity.updateContent(content);
        return entity.toDomain();
    }

    @Override
    public TechBlogComment softDelete(Long commentId, Long deletedByMemberId, boolean admin) {
        TechBlogCommentJpaEntity entity = getCommentEntity(commentId);
        entity.softDelete(deletedByMemberId, admin);
        commentLikeJpaRepository.deleteByCommentId(commentId);
        return entity.toDomain();
    }

    @Override
    public void hardDelete(Long commentId) {
        commentJpaRepository.deleteById(commentId);
    }

    @Override
    public TechBlogLikeInfo toggleCommentLike(Long commentId, Long memberId) {
        boolean liked;
        if (commentLikeJpaRepository.existsByCommentIdAndMemberId(commentId, memberId)) {
            commentLikeJpaRepository.deleteByCommentIdAndMemberId(commentId, memberId);
            liked = false;
        } else {
            commentLikeJpaRepository.save(new TechBlogCommentLikeJpaEntity(commentId, memberId));
            liked = true;
        }
        return new TechBlogLikeInfo(liked, commentLikeJpaRepository.countByCommentId(commentId));
    }

    private TechBlogCommentJpaEntity getCommentEntity(Long commentId) {
        return commentJpaRepository.findById(commentId)
            .orElseThrow(() -> new TechBlogDomainException(TechBlogErrorCode.COMMENT_NOT_FOUND));
    }
}
