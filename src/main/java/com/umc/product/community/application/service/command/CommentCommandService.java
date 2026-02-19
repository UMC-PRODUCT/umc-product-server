package com.umc.product.community.application.service.command;

import com.umc.product.community.application.port.in.command.comment.CreateCommentUseCase;
import com.umc.product.community.application.port.in.command.comment.DeleteCommentUseCase;
import com.umc.product.community.application.port.in.command.comment.ToggleCommentLikeUseCase;
import com.umc.product.community.application.port.in.command.comment.dto.CreateCommentCommand;
import com.umc.product.community.application.port.in.query.dto.CommentInfo;
import com.umc.product.community.application.port.out.comment.LoadCommentPort;
import com.umc.product.community.application.port.out.comment.SaveCommentPort;
import com.umc.product.community.application.port.out.post.LoadPostPort;
import com.umc.product.community.application.service.AuthorInfoProvider;
import com.umc.product.community.domain.Comment;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandService implements CreateCommentUseCase, DeleteCommentUseCase, ToggleCommentLikeUseCase {

    private final LoadPostPort loadPostPort;
    private final LoadCommentPort loadCommentPort;
    private final SaveCommentPort saveCommentPort;
    private final AuthorInfoProvider authorInfoProvider;

    @Override
    public CommentInfo create(CreateCommentCommand command) {
        loadPostPort.findById(command.postId())
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        Comment comment = Comment.create(
            command.postId(),
            command.challengerId(),
            command.content(),
            command.parentId()
        );

        Comment savedComment = saveCommentPort.save(comment);

        String challengerName = authorInfoProvider.getAuthorName(command.challengerId());
        return CommentInfo.from(savedComment, challengerName);
    }

    @Override
    public void delete(Long commentId, Long challengerId) {
        Comment comment = loadCommentPort.findById(commentId)
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getChallengerId().equals(challengerId)) {
            throw new BusinessException(Domain.COMMUNITY, CommunityErrorCode.COMMENT_NOT_OWNED);
        }

        saveCommentPort.delete(comment);
    }

    @Override
    public LikeResult toggle(Long commentId, Long challengerId) {
        loadCommentPort.findById(commentId)
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.COMMENT_NOT_FOUND));

        return saveCommentPort.toggleLike(commentId, challengerId);
    }
}
