package com.umc.product.community.application.port.in.post;

import com.umc.product.community.application.port.in.post.Command.CreateCommentCommand;

public interface CreateCommentUseCase {
    CommentInfo create(CreateCommentCommand command);
}
