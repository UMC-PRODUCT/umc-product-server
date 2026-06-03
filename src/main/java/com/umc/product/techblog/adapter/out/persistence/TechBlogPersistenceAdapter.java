package com.umc.product.techblog.adapter.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.umc.product.techblog.application.port.out.LoadTechBlogCommentPort;
import com.umc.product.techblog.application.port.out.LoadTechBlogContentPort;
import com.umc.product.techblog.application.port.out.LoadTechBlogLikePort;
import com.umc.product.techblog.application.port.out.SaveTechBlogCommentPort;
import com.umc.product.techblog.application.port.out.SaveTechBlogContentPort;
import com.umc.product.techblog.application.port.out.SaveTechBlogLikePort;
import com.umc.product.techblog.domain.TechBlogComment;
import com.umc.product.techblog.domain.TechBlogCommentLike;
import com.umc.product.techblog.domain.TechBlogCommentSort;
import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentLike;
import com.umc.product.techblog.domain.TechBlogContentType;
import com.umc.product.techblog.domain.TechBlogDomainException;
import com.umc.product.techblog.domain.TechBlogErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TechBlogPersistenceAdapter implements LoadTechBlogContentPort, SaveTechBlogContentPort,
    LoadTechBlogCommentPort, SaveTechBlogCommentPort, LoadTechBlogLikePort, SaveTechBlogLikePort {

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
    public int countContentLikes(Long contentId) {
        return contentLikeJpaRepository.countByContentId(contentId);
    }

    @Override
    public boolean existsContentLike(Long contentId, Long memberId) {
        return memberId != null && contentLikeJpaRepository.existsByContentIdAndMemberId(contentId, memberId);
    }

    @Override
    public TechBlogContent save(TechBlogContent content) {
        if (content.getId() != null) {
            return contentJpaRepository.save(content);
        }
        contentJpaRepository.insertIgnore(content.getContentType().name(), content.getSlug());
        return contentJpaRepository.findByContentTypeAndSlug(content.getContentType(), content.getSlug())
            .orElseThrow(() -> new TechBlogDomainException(TechBlogErrorCode.CONTENT_NOT_FOUND));
    }

    @Override
    public TechBlogContentLike saveContentLike(TechBlogContentLike like) {
        return contentLikeJpaRepository.save(like);
    }

    @Override
    public void deleteContentLike(Long contentId, Long memberId) {
        contentLikeJpaRepository.deleteByContentIdAndMemberId(contentId, memberId);
    }

    @Override
    public Optional<TechBlogComment> findById(Long commentId) {
        return commentJpaRepository.findById(commentId);
    }

    @Override
    public TechBlogComment getByIdAndContentId(Long commentId, Long contentId) {
        return commentJpaRepository.findByIdAndContentId(commentId, contentId)
            .orElseThrow(() -> new TechBlogDomainException(TechBlogErrorCode.COMMENT_NOT_FOUND));
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
    public int countCommentLikes(Long commentId) {
        return commentLikeJpaRepository.countByCommentId(commentId);
    }

    @Override
    public Map<Long, Integer> countCommentLikesByCommentIds(List<Long> commentIds) {
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
    public void hardDelete(Long commentId) {
        commentJpaRepository.deleteById(commentId);
    }

    @Override
    public boolean existsCommentLike(Long commentId, Long memberId) {
        return memberId != null && commentLikeJpaRepository.existsByCommentIdAndMemberId(commentId, memberId);
    }

    @Override
    public TechBlogCommentLike saveCommentLike(TechBlogCommentLike like) {
        return commentLikeJpaRepository.save(like);
    }

    @Override
    public void deleteCommentLike(Long commentId, Long memberId) {
        commentLikeJpaRepository.deleteByCommentIdAndMemberId(commentId, memberId);
    }

    @Override
    public void deleteCommentLikesByCommentId(Long commentId) {
        commentLikeJpaRepository.deleteByCommentId(commentId);
    }

}
