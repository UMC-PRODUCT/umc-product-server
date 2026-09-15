package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.command.dto.DeleteBlogSeriesCommand;

public interface DeleteBlogSeriesUseCase {

    void delete(DeleteBlogSeriesCommand command);
}
