package com.umc.product.community.application.port.in.command.comment;

import com.umc.product.community.application.port.in.command.comment.dto.CreateCommentCommand;
import com.umc.product.community.application.port.in.query.dto.CommentInfo;

public interface CreateCommentUseCase {
    CommentInfo create(CreateCommentCommand command);
}
