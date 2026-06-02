package com.umc.product.techblog.application.port.in.command;

import com.umc.product.techblog.application.port.in.command.dto.UpdateTechBlogCommentCommand;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentInfo;

public interface UpdateTechBlogCommentUseCase {

    TechBlogCommentInfo update(UpdateTechBlogCommentCommand command);
}
