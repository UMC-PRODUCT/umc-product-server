package com.umc.product.techblog.application.port.in.command;

import com.umc.product.techblog.application.port.in.command.dto.CreateTechBlogCommentCommand;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentInfo;

public interface CreateTechBlogCommentUseCase {

    TechBlogCommentInfo create(CreateTechBlogCommentCommand command);
}
