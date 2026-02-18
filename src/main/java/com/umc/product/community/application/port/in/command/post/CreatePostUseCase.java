package com.umc.product.community.application.port.in.command.post;

import com.umc.product.community.application.port.in.command.post.dto.CreateLightningCommand;
import com.umc.product.community.application.port.in.command.post.dto.CreatePostCommand;
import com.umc.product.community.application.port.in.command.post.dto.PostInfo;

public interface CreatePostUseCase {
    PostInfo createPost(CreatePostCommand command);

    PostInfo createLightningPost(CreateLightningCommand command);
}
