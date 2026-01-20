package com.umc.product.community.application.port.in.post;

import com.umc.product.community.application.port.in.post.command.CreateCommentCommand;

public interface CreateCommentUseCase {
    CommentInfo create(CreateCommentCommand command);
}
