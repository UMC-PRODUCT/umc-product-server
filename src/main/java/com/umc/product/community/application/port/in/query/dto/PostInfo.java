package com.umc.product.community.application.port.in.query.dto;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import com.umc.product.member.application.port.in.query.MemberInfo;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PostInfo(
    Long postId,
    String title,
    String content,
    Category category,
    Long authorMemberId, // Author Challenger ID임 주의
    Long authorChallengerId, // Author Challenger ID임 주의
    String authorName,
    String authorNickname,
    String authorProfileImage,
    ChallengerPart authorPart,
    Instant meetAt,
    String location,
    Integer maxParticipants,
    String openChatUrl,
    Instant createdAt,
    int commentCount,
    int likeCount,
    boolean isLiked, // Deprecated
    boolean isAuthor // Deprecated
) {

    public static PostInfo from(Post post, MemberInfo memberInfo, ChallengerInfo challengerInfo) {
        Long authorChallengerId = post.getAuthorChallengerId();

        Long authorMemberId = memberInfo != null ? memberInfo.id() : null;
        String authorName = memberInfo != null ? memberInfo.name() : null;
        String authorNickname = memberInfo != null ? memberInfo.nickname() : null;
        String authorProfileImage = memberInfo != null ? memberInfo.profileImageLink() : null;
        ChallengerPart authorPart = challengerInfo != null ? challengerInfo.part() : null;

        PostInfoBuilder builder = PostInfo.builder();

        // 번개글인 경우
        if (post.isLightning()) {
            Post.LightningInfo info = post.getLightningInfoOrThrow();
            builder
                .meetAt(info.meetAt())
                .location(info.location())
                .maxParticipants(info.maxParticipants())
                .openChatUrl(info.openChatUrl());
        }

        return builder
            .postId(post.getPostId().id())
            .title(post.getTitle())
            .content(post.getContent())
            .category(post.getCategory())
            .authorMemberId(authorMemberId)
            .authorChallengerId(post.getAuthorChallengerId())
            .authorName(authorName)
            .authorNickname(authorNickname)
            .authorProfileImage(authorProfileImage)
            .authorPart(authorPart)
            .createdAt(post.getCreatedAt())
            .likeCount(post.getLikeCount())
            .isLiked(post.isLiked())
            .isAuthor(false) // TODO: deprecate 예정
            .build();
    }

    // ======================================================
    // ====================== 예은 작업본 ======================
    // ======================================================

    @Deprecated
    public static PostInfo from(Post post, Long authorId, String authorName) {

        return from(post, authorId, authorName, null, null, 0, false);
    }

    @Deprecated
    public static PostInfo from(Post post, Long authorId, String authorName, int commentCount) {

        return from(post, authorId, authorName, null, null, commentCount, false);
    }

    @Deprecated
    public static PostInfo from(
        Post post, Long authorId, String authorName,
        String authorProfileImage, int commentCount) {

        return from(post, authorId, authorName, authorProfileImage, null, commentCount, false);
    }

    @Deprecated
    public static PostInfo from(
        Post post, Long authorId, String authorName, String authorProfileImage,
        ChallengerPart authorPart, int commentCount) {

        return from(post, authorId, authorName, authorProfileImage, authorPart, commentCount, false);
    }

    @Deprecated
    public static PostInfo from(
        Post post, Long authorId, String authorName, String authorProfileImage,
        ChallengerPart authorPart, int commentCount, boolean isAuthor) {

        Long postId = post.getPostId() != null ? post.getPostId().id() : null;

        // 번개글인 경우
        if (post.isLightning()) {
            Post.LightningInfo info = post.getLightningInfoOrThrow();
            return PostInfo.builder()
                .postId(postId)
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .authorChallengerId(authorId)
                .authorName(authorName)
                .authorProfileImage(authorProfileImage)
                .authorPart(authorPart)
                .meetAt(info.meetAt())
                .location(info.location())
                .maxParticipants(info.maxParticipants())
                .openChatUrl(info.openChatUrl())
                .createdAt(post.getCreatedAt())
                .commentCount(commentCount)
                .likeCount(post.getLikeCount())
                .isLiked(post.isLiked())
                .isAuthor(isAuthor)
                .build();
        }

        // 일반 게시글
        return PostInfo.builder()
            .postId(postId)
            .title(post.getTitle())
            .content(post.getContent())
            .category(post.getCategory())
            .authorChallengerId(authorId)
            .authorName(authorName)
            .authorProfileImage(authorProfileImage)
            .authorPart(authorPart)
            .createdAt(post.getCreatedAt())
            .commentCount(commentCount)
            .likeCount(post.getLikeCount())
            .isLiked(post.isLiked())
            .isAuthor(isAuthor)
            .build();
    }
}
