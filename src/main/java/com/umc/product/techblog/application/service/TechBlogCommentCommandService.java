package com.umc.product.techblog.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.techblog.application.port.in.command.CreateTechBlogCommentUseCase;
import com.umc.product.techblog.application.port.in.command.DeleteTechBlogCommentUseCase;
import com.umc.product.techblog.application.port.in.command.ToggleTechBlogCommentLikeUseCase;
import com.umc.product.techblog.application.port.in.command.UpdateTechBlogCommentUseCase;
import com.umc.product.techblog.application.port.in.command.dto.CreateTechBlogCommentCommand;
import com.umc.product.techblog.application.port.in.command.dto.DeleteTechBlogCommentCommand;
import com.umc.product.techblog.application.port.in.command.dto.UpdateTechBlogCommentCommand;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentInfo;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogLikeInfo;
import com.umc.product.techblog.application.port.out.LoadTechBlogCommentPort;
import com.umc.product.techblog.application.port.out.LoadTechBlogContentPort;
import com.umc.product.techblog.application.port.out.SaveTechBlogCommentPort;
import com.umc.product.techblog.application.port.out.SaveTechBlogContentPort;
import com.umc.product.techblog.domain.TechBlogComment;
import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentType;
import com.umc.product.techblog.domain.TechBlogDomainException;
import com.umc.product.techblog.domain.TechBlogErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TechBlogCommentCommandService implements CreateTechBlogCommentUseCase, UpdateTechBlogCommentUseCase,
    DeleteTechBlogCommentUseCase, ToggleTechBlogCommentLikeUseCase {

    private final LoadTechBlogContentPort loadTechBlogContentPort;
    private final SaveTechBlogContentPort saveTechBlogContentPort;
    private final LoadTechBlogCommentPort loadTechBlogCommentPort;
    private final SaveTechBlogCommentPort saveTechBlogCommentPort;
    private final TechBlogCommentInfoAssembler commentInfoAssembler;

    @Override
    public TechBlogCommentInfo create(CreateTechBlogCommentCommand command) {
        TechBlogContent content = getOrCreateContent(command.type(), command.slug());
        validateParent(content.getId(), command.parentCommentId());

        TechBlogComment comment = TechBlogComment.create(
            content.getId(),
            command.parentCommentId(),
            command.authorMemberId(),
            command.anonymous(),
            command.nickname(),
            command.content()
        );
        TechBlogComment saved = saveTechBlogCommentPort.save(comment);
        return commentInfoAssembler.assemble(saved, false, 0, List.of());
    }

    @Override
    public TechBlogCommentInfo update(UpdateTechBlogCommentCommand command) {
        TechBlogContent content = getContent(command.type(), command.slug());
        TechBlogComment comment = getCommentInContent(command.commentId(), content.getId());
        validateOwner(comment, command.memberId());
        comment.updateContent(command.content());

        TechBlogComment updated = saveTechBlogCommentPort.updateContent(comment.getId(), comment.getContent());
        return assembleSingle(updated, command.memberId());
    }

    @Override
    public void delete(DeleteTechBlogCommentCommand command) {
        TechBlogContent content = getContent(command.type(), command.slug());
        TechBlogComment comment = getCommentInContent(command.commentId(), content.getId());
        validateOwner(comment, command.memberId());
        deleteResolved(comment, command.memberId(), false);
    }

    @Override
    public void deleteByAdmin(Long commentId, Long adminMemberId) {
        TechBlogComment comment = loadTechBlogCommentPort.findById(commentId)
            .orElseThrow(() -> new TechBlogDomainException(TechBlogErrorCode.COMMENT_NOT_FOUND));
        deleteResolved(comment, adminMemberId, true);
    }

    @Override
    public TechBlogLikeInfo toggle(String type, String slug, Long commentId, Long memberId) {
        TechBlogContent content = getContent(type, slug);
        TechBlogComment comment = getCommentInContent(commentId, content.getId());
        comment.ensureNotDeleted();
        return saveTechBlogCommentPort.toggleCommentLike(comment.getId(), memberId);
    }

    private void validateParent(Long contentId, Long parentCommentId) {
        if (parentCommentId == null) {
            return;
        }

        TechBlogComment parent = getCommentInContent(parentCommentId, contentId);
        parent.ensureNotDeleted();
        if (parent.getParentCommentId() != null) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_PARENT_COMMENT);
        }
    }

    private void deleteResolved(TechBlogComment comment, Long memberId, boolean admin) {
        if (loadTechBlogCommentPort.existsVisibleReply(comment.getId())) {
            saveTechBlogCommentPort.softDelete(comment.getId(), memberId, admin);
            return;
        }
        saveTechBlogCommentPort.hardDelete(comment.getId());
    }

    private TechBlogCommentInfo assembleSingle(TechBlogComment comment, Long viewerMemberId) {
        List<Long> commentIds = List.of(comment.getId());
        int likeCount = loadTechBlogCommentPort.countLikesByCommentIds(commentIds)
            .getOrDefault(comment.getId(), 0);
        boolean likedByMe = loadTechBlogCommentPort.findLikedCommentIds(commentIds, viewerMemberId)
            .contains(comment.getId());

        return commentInfoAssembler.assemble(comment, likedByMe, likeCount, List.of());
    }

    private TechBlogContent getOrCreateContent(String typeValue, String slug) {
        TechBlogContentType type = TechBlogContentType.fromPath(typeValue);
        return loadTechBlogContentPort.findByTypeAndSlug(type, slug)
            .orElseGet(() -> saveTechBlogContentPort.save(TechBlogContent.create(type, slug)));
    }

    private TechBlogContent getContent(String typeValue, String slug) {
        TechBlogContentType type = TechBlogContentType.fromPath(typeValue);
        return loadTechBlogContentPort.findByTypeAndSlug(type, slug)
            .orElseThrow(() -> new TechBlogDomainException(TechBlogErrorCode.CONTENT_NOT_FOUND));
    }

    private TechBlogComment getCommentInContent(Long commentId, Long contentId) {
        return loadTechBlogCommentPort.findByIdAndContentId(commentId, contentId)
            .orElseThrow(() -> new TechBlogDomainException(TechBlogErrorCode.COMMENT_NOT_FOUND));
    }

    private void validateOwner(TechBlogComment comment, Long memberId) {
        if (comment.getAuthorMemberId() == null || !comment.getAuthorMemberId().equals(memberId)) {
            throw new TechBlogDomainException(TechBlogErrorCode.COMMENT_NOT_OWNED);
        }
        comment.ensureNotDeleted();
    }
}
