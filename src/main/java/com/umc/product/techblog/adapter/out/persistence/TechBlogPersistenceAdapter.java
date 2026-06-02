package com.umc.product.techblog.adapter.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogCommentLikeJpaEntity;
import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogContentLikeJpaEntity;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogLikeInfo;
import com.umc.product.techblog.application.port.out.LoadTechBlogCommentPort;
import com.umc.product.techblog.application.port.out.LoadTechBlogContentPort;
import com.umc.product.techblog.application.port.out.SaveTechBlogCommentPort;
import com.umc.product.techblog.application.port.out.SaveTechBlogContentPort;
import com.umc.product.techblog.domain.TechBlogComment;
import com.umc.product.techblog.domain.TechBlogCommentSort;
import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentType;
import com.umc.product.techblog.domain.TechBlogDomainException;
import com.umc.product.techblog.domain.TechBlogErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TechBlogPersistenceAdapter implements LoadTechBlogContentPort, SaveTechBlogContentPort,
    LoadTechBlogCommentPort, SaveTechBlogCommentPort {

    private final TechBlogContentJpaRepository contentJpaRepository;
    private final TechBlogContentLikeJpaRepository contentLikeJpaRepository;
    private final TechBlogCommentJpaRepository commentJpaRepository;
    private final TechBlogCommentLikeJpaRepository commentLikeJpaRepository;
    private final TechBlogCommentQueryRepository commentQueryRepository;
    private final TechBlogCommentLikeQueryRepository commentLikeQueryRepository;

    @Override
    public Optional<TechBlogContent> findByTypeAndSlug(TechBlogContentType type, String slug) {
        return contentJpaRepository.findByContentTypeAndSlug(type, slug);
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
        return contentJpaRepository.save(content);
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
        return commentJpaRepository.findById(commentId);
    }

    @Override
    public Optional<TechBlogComment> findByIdAndContentId(Long commentId, Long contentId) {
        return commentJpaRepository.findByIdAndContentId(commentId, contentId);
    }

    @Override
    public List<TechBlogComment> listTopLevel(Long contentId, TechBlogCommentSort sort, Long cursor, int size) {
        return commentQueryRepository.listTopLevel(contentId, sort, cursor, size);
    }

    @Override
    public List<TechBlogComment> listRepliesByParentIds(List<Long> parentCommentIds) {
        return commentQueryRepository.listRepliesByParentIds(parentCommentIds);
    }

    @Override
    public boolean existsVisibleReply(Long parentCommentId) {
        return commentQueryRepository.existsVisibleReply(parentCommentId);
    }

    @Override
    public Map<Long, Integer> countLikesByCommentIds(List<Long> commentIds) {
        return commentLikeQueryRepository.countByCommentIds(commentIds);
    }

    @Override
    public Set<Long> findLikedCommentIds(List<Long> commentIds, Long memberId) {
        return commentLikeQueryRepository.findLikedCommentIds(commentIds, memberId);
    }

    @Override
    public TechBlogComment save(TechBlogComment comment) {
        return commentJpaRepository.save(comment);
    }

    @Override
    public TechBlogComment updateContent(Long commentId, String content) {
        TechBlogComment entity = getCommentEntity(commentId);
        entity.updateContent(content);
        return entity;
    }

    @Override
    public TechBlogComment softDelete(Long commentId, Long deletedByMemberId, boolean admin) {
        TechBlogComment entity = getCommentEntity(commentId);
        if (admin) {
            entity.deleteByAdmin(deletedByMemberId);
        } else {
            entity.deleteByUser(deletedByMemberId);
        }
        commentLikeJpaRepository.deleteByCommentId(commentId);
        return entity;
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

    private TechBlogComment getCommentEntity(Long commentId) {
        return commentJpaRepository.findById(commentId)
            .orElseThrow(() -> new TechBlogDomainException(TechBlogErrorCode.COMMENT_NOT_FOUND));
    }
}
