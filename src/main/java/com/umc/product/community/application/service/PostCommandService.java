package com.umc.product.community.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
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
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
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
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public PostInfo createPost(CreatePostCommand command) {
        Post post = Post.createPost(
                command.title(),
                command.content(),
                command.category()
        );

        Post savedPost = savePostPort.save(post, command.authorChallengerId());
        String authorName = getAuthorName(command.authorChallengerId());
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
                lightningInfo
        );

        Post savedPost = savePostPort.save(post, command.authorChallengerId());
        String authorName = getAuthorName(command.authorChallengerId());
        return PostInfo.from(savedPost, command.authorChallengerId(), authorName);
    }

    @Override
    public PostInfo updatePost(UpdatePostCommand command) {
        Post post = loadPostPort.findById(command.postId())
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        post.update(
                command.title(),
                command.content(),
                command.category()
        );

        Post savedPost = savePostPort.save(post);
        Long authorId = loadPostPort.findAuthorIdByPostId(command.postId());
        String authorName = getAuthorName(authorId);
        return PostInfo.from(savedPost, authorId, authorName);
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

    private String getAuthorName(Long challengerId) {
        ChallengerInfo challengerInfo = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        MemberProfileInfo profileInfo = getMemberUseCase.getProfile(challengerInfo.memberId());
        return profileInfo.name();
    }
}
