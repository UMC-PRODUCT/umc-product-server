package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.command.dto.UpdateBlogSeriesCommand;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesInfo;

public interface UpdateBlogSeriesUseCase {

    BlogSeriesInfo update(UpdateBlogSeriesCommand command);
}
