package com.umc.product.community.application.port.in.post;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.Command.CreateLightningCommand;
import com.umc.product.community.application.port.in.post.Command.CreatePostCommand;

public interface CreatePostUseCase {
    PostInfo createPost(CreatePostCommand command);

    PostInfo createLightningPost(CreateLightningCommand command);
}
