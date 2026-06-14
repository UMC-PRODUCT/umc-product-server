package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.command.dto.CreateBlogSeriesCommand;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesInfo;

public interface CreateBlogSeriesUseCase {

    BlogSeriesInfo create(CreateBlogSeriesCommand command);
}
