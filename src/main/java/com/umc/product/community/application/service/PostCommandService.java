package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.CreatePostUseCase;
import com.umc.product.community.application.port.in.post.DeletePostUseCase;
import com.umc.product.community.application.port.in.post.TogglePostLikeUseCase;
import com.umc.product.community.application.port.in.post.ToggleScrapUseCase;
import com.umc.product.community.application.port.in.post.UpdateLightningUseCase;
import com.umc.product.community.application.port.in.post.UpdatePostUseCase;
import com.umc.product.community.application.port.in.post.command.CreateLightningCommand;
import com.umc.product.community.application.port.in.post.command.CreatePostCommand;
import com.umc.product.community.application.port.in.post.command.UpdateLightningCommand;
import com.umc.product.community.application.port.in.post.command.UpdatePostCommand;
import com.umc.product.community.application.port.out.LoadPostPort;
import com.umc.product.community.application.port.out.LoadScrapPort;
import com.umc.product.community.application.port.out.PostWithAuthor;
import com.umc.product.community.application.port.out.SavePostPort;
import com.umc.product.community.application.port.out.SaveScrapPort;
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
public class PostCommandService implements CreatePostUseCase, UpdatePostUseCase, UpdateLightningUseCase,
        DeletePostUseCase, TogglePostLikeUseCase, ToggleScrapUseCase {

    private final LoadPostPort loadPostPort;
    private final SavePostPort savePostPort;
    private final LoadScrapPort loadScrapPort;
    private final SaveScrapPort saveScrapPort;
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

    @Override
    public ScrapResult toggleScrap(Long postId, Long challengerId) {
        // 게시글 존재 확인
        loadPostPort.findById(postId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        // 스크랩 토글
        boolean scrapped = saveScrapPort.toggleScrap(postId, challengerId);

        return new ScrapResult(scrapped);
    }
}
