package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.command.dto.ReplaceBlogSeriesContentsCommand;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesInfo;

public interface ReplaceBlogSeriesContentsUseCase {

    BlogSeriesInfo replaceContents(ReplaceBlogSeriesContentsCommand command);
}
