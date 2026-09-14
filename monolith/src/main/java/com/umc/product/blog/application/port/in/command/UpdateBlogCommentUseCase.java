package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.command.dto.UpdateBlogCommentCommand;
import com.umc.product.blog.application.port.in.query.dto.BlogCommentInfo;

public interface UpdateBlogCommentUseCase {

    BlogCommentInfo update(UpdateBlogCommentCommand command);
}
