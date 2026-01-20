package com.umc.product.community.application.port.in.post;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.command.CreateLightningCommand;
import com.umc.product.community.application.port.in.post.command.CreatePostCommand;

public interface CreatePostUseCase {
    PostInfo createPost(CreatePostCommand command);

    PostInfo createLightningPost(CreateLightningCommand command);
}
