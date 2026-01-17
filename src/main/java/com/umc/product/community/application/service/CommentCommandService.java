package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.post.Command.CreateCommentCommand;
import com.umc.product.community.application.port.in.post.CommentInfo;
import com.umc.product.community.application.port.in.post.CreateCommentUseCase;
import com.umc.product.community.application.port.in.post.DeleteCommentUseCase;
import com.umc.product.community.application.port.in.post.ToggleCommentLikeUseCase;
import com.umc.product.community.application.port.out.SaveCommentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandService implements CreateCommentUseCase, DeleteCommentUseCase, ToggleCommentLikeUseCase {

    private final SaveCommentPort saveCommentPort;

    @Override
    public CommentInfo create(CreateCommentCommand command) {
        return null;
    }

    @Override
    public void delete(Long commentId, Long challengerId) {
    }

    @Override
    public LikeResult toggle(Long commentId, Long challengerId) {
        return null;
    }
}
