package com.umc.product.community.application.port.in.post;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.command.UpdatePostCommand;

public interface UpdatePostUseCase {
    PostInfo updatePost(UpdatePostCommand command);
}
