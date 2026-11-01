package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.command.dto.CreateBlogCommentCommand;
import com.umc.product.blog.application.port.in.query.dto.BlogCommentInfo;

public interface CreateBlogCommentUseCase {

    BlogCommentInfo create(CreateBlogCommentCommand command);
}
