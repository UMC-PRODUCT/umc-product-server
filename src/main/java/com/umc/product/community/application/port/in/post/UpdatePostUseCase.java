package com.umc.product.community.application.port.in.post;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.Command.UpdatePostCommand;

public interface UpdatePostUseCase {
    PostInfo updatePost(Long postId, UpdatePostCommand command);
}
