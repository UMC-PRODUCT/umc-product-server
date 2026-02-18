package com.umc.product.community.application.service.command;

import com.umc.product.community.application.port.in.command.post.CreatePostUseCase;
import com.umc.product.community.application.port.in.command.post.DeletePostUseCase;
import com.umc.product.community.application.port.in.command.post.TogglePostLikeUseCase;
import com.umc.product.community.application.port.in.command.post.UpdateLightningUseCase;
import com.umc.product.community.application.port.in.command.post.UpdatePostUseCase;
import com.umc.product.community.application.port.in.command.post.dto.CreateLightningCommand;
import com.umc.product.community.application.port.in.command.post.dto.CreatePostCommand;
import com.umc.product.community.application.port.in.command.post.dto.PostInfo;
import com.umc.product.community.application.port.in.command.post.dto.UpdateLightningCommand;
import com.umc.product.community.application.port.in.command.post.dto.UpdatePostCommand;
import com.umc.product.community.application.port.out.dto.PostWithAuthor;
import com.umc.product.community.application.port.out.post.LoadPostPort;
import com.umc.product.community.application.port.out.post.SavePostPort;
import com.umc.product.community.application.service.AuthorInfoProvider;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.Post.LightningInfo;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandService implements CreatePostUseCase, UpdatePostUseCase, UpdateLightningUseCase,
    DeletePostUseCase, TogglePostLikeUseCase {

    private final LoadPostPort loadPostPort;
    private final SavePostPort savePostPort;
    private final AuthorInfoProvider authorInfoProvider;

    @Override
    public PostInfo createPost(CreatePostCommand command) {
        Post post = Post.createPost(
            command.title(),
            command.content(),
            command.category(),
            command.authorChallengerId()
        );

        Post savedPost = savePostPort.save(post, command.authorChallengerId());
        String authorName = authorInfoProvider.getAuthorName(command.authorChallengerId());
        return PostInfo.from(savedPost, command.authorChallengerId(), authorName);
    }

    @Override
    public PostInfo createLightningPost(CreateLightningCommand command) {
        LightningInfo lightningInfo = new LightningInfo(
            command.meetAt(),
            command.location(),
            command.maxParticipants(),
            command.openChatUrl()
        );

        // Service 레이어에서 시간 검증 (테스트 용이성)
        lightningInfo.validateMeetAtIsFuture(LocalDateTime.now());

        Post post = Post.createLightning(
            command.title(),
            command.content(),
            lightningInfo,
            command.authorChallengerId()
        );

        Post savedPost = savePostPort.save(post, command.authorChallengerId());
        String authorName = authorInfoProvider.getAuthorName(command.authorChallengerId());
        return PostInfo.from(savedPost, command.authorChallengerId(), authorName);
    }

    @Override
    public PostInfo updatePost(UpdatePostCommand command) {
        PostWithAuthor postWithAuthor = loadPostPort.findByIdWithAuthor(command.postId())
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        Post post = postWithAuthor.post();
        post.update(
            command.title(),
            command.content(),
            command.category()
        );

        Post savedPost = savePostPort.save(post);
        String authorName = authorInfoProvider.getAuthorName(postWithAuthor.authorChallengerId());
        return PostInfo.from(savedPost, postWithAuthor.authorChallengerId(), authorName);
    }

    @Override
    public PostInfo updateLightning(UpdateLightningCommand command) {
        PostWithAuthor postWithAuthor = loadPostPort.findByIdWithAuthor(command.postId())
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        Post post = postWithAuthor.post();

        LightningInfo lightningInfo = new LightningInfo(
            command.meetAt(),
            command.location(),
            command.maxParticipants(),
            command.openChatUrl()
        );

        // Service 레이어에서 시간 검증 (테스트 용이성)
        lightningInfo.validateMeetAtIsFuture(LocalDateTime.now());

        post.updateLightning(
            command.title(),
            command.content(),
            lightningInfo
        );

        Post savedPost = savePostPort.save(post);
        String authorName = authorInfoProvider.getAuthorName(postWithAuthor.authorChallengerId());
        return PostInfo.from(savedPost, postWithAuthor.authorChallengerId(), authorName);
    }

    @Override
    public void deletePost(Long postId) {
        loadPostPort.findById(postId)
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        savePostPort.deleteById(postId);
    }

    @Override
    public LikeResult toggleLike(Long postId, Long challengerId) {
        loadPostPort.findById(postId)
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        return savePostPort.toggleLike(postId, challengerId);
    }
}
