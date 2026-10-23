package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.command.dto.CreateBlogContentCommand;
import com.umc.product.blog.application.port.in.query.dto.BlogContentInfo;

public interface CreateBlogContentUseCase {

    BlogContentInfo create(CreateBlogContentCommand command);
}
