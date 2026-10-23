package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.command.dto.UpdateBlogContentCommand;
import com.umc.product.blog.application.port.in.query.dto.BlogContentInfo;

public interface UpdateBlogContentUseCase {

    BlogContentInfo update(UpdateBlogContentCommand command);
}
