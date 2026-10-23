package com.umc.product.community.application.port.in.command.post;

import com.umc.product.community.application.port.in.command.post.dto.UpdatePostCommand;
import com.umc.product.community.application.port.in.query.dto.PostInfo;

public interface UpdatePostUseCase {
    PostInfo updatePost(UpdatePostCommand command);
}
