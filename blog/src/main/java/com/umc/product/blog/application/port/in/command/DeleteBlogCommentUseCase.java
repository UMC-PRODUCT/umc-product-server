package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.command.dto.DeleteBlogCommentCommand;

public interface DeleteBlogCommentUseCase {

    void delete(DeleteBlogCommentCommand command);
}
