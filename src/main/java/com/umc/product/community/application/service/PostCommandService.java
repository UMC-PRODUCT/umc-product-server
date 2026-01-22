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
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.Post.LightningInfo;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
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
        Post post = Post.createPost(
                command.title(),
                command.content(),
                command.category(),
                command.region(),
                command.anonymous()
        );

        Post savedPost = savePostPort.save(post);
        return PostInfo.from(savedPost);
    }

    @Override
    public PostInfo createLightningPost(CreateLightningCommand command) {
        LightningInfo lightningInfo = new LightningInfo(
                command.meetAt(),
                command.location(),
                command.maxParticipants()
        );

        Post post = Post.createLightning(
                command.title(),
                command.content(),
                command.region(),
                command.anonymous(),
                lightningInfo
        );

        Post savedPost = savePostPort.save(post);
        return PostInfo.from(savedPost);
    }

    @Override
    public PostInfo updatePost(UpdatePostCommand command) {
        Post post = loadPostPort.findById(command.postId())
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        post.update(
                command.title(),
                command.content(),
                command.category(),
                command.region()
        );

        Post savedPost = savePostPort.save(post);
        return PostInfo.from(savedPost);
    }

    @Override
    public void deletePost(Long postId) {
        loadPostPort.findById(postId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        savePostPort.deleteById(postId);
    }

    @Override
    public LikeResult toggle(Long postId, Long challengerId) {
        loadPostPort.findById(postId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        return savePostPort.toggleLike(postId, challengerId);
    }
}
