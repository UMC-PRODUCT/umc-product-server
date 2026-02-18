package com.umc.product.community.application.port.in.command.comment;

import com.umc.product.community.application.port.in.command.comment.dto.CommentInfo;
import com.umc.product.community.application.port.in.command.comment.dto.CreateCommentCommand;

public interface CreateCommentUseCase {
    CommentInfo create(CreateCommentCommand command);
}
