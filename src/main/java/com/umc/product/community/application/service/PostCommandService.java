package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.CreatePostUseCase;
import com.umc.product.community.application.port.in.post.DeletePostUseCase;
import com.umc.product.community.application.port.in.post.TogglePostLikeUseCase;
import com.umc.product.community.application.port.in.post.UpdatePostUseCase;
import com.umc.product.community.application.port.in.post.command.CreateLightningCommand;
import com.umc.product.community.application.port.in.post.command.CreatePostCommand;
import com.umc.product.community.application.port.in.post.command.UpdatePostCommand;
import com.umc.product.community.application.port.out.LoadPostPort;
import com.umc.product.community.application.port.out.SavePostPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandService implements CreatePostUseCase, UpdatePostUseCase, DeletePostUseCase,
        TogglePostLikeUseCase {

    private final LoadPostPort loadPostPort;
    private final SavePostPort savePostPort;

    @Override
    public PostInfo createPost(CreatePostCommand command) {
        return null;
        // TODO: 구현 필요
    }

    @Override
    public PostInfo createLightningPost(CreateLightningCommand command) {
        return null;
        // TODO: 구현 필요
    }

    @Override
    public PostInfo updatePost(UpdatePostCommand command) {
        return null;
        // TODO: 구현 필요
    }

    @Override
    public void deletePost(Long postId) {
        // TODO: 구현 필요
    }

    @Override
    public LikeResult toggle(Long postId, Long challengerId) {
        return null;
    }
}
