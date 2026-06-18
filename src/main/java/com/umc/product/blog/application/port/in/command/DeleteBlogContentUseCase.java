package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.command.dto.DeleteBlogContentCommand;

public interface DeleteBlogContentUseCase {

    void delete(DeleteBlogContentCommand command);
}
