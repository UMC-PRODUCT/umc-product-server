package com.umc.product.community.application.port.in.query.dto;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import com.umc.product.member.application.port.in.query.MemberInfo;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PostInfo(
    Long postId,
    String title,
    String content,
    Category category,
    Long authorChallengerId, // Author Challenger ID임 주의
    String authorName,
    String authorProfileImage,
    ChallengerPart authorPart,
    LocalDateTime meetAt,
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

        return PostInfo.builder()
            .postId(post.getPostId().id())
            .title(post.getTitle())
            .content(post.getContent())
            .category(post.getCategory())
            .authorChallengerId(post.getAuthorChallengerId())
            .authorName(memberInfo.name())
            .authorProfileImage(memberInfo.profileImageLink())
            .authorPart(challengerInfo.part())
            //        LocalDateTime meetAt,
            //        String location,
            //        Integer maxParticipants,
            //        String openChatUrl,
            .createdAt(post.getCreatedAt())
            .likeCount(post.getLikeCount())
            .isLiked(post.isLiked())
            .isAuthor(authorChallengerId.equals(challengerInfo.challengerId()))
            .build();
    }

    // ======================================================
    // ====================== 예은 작업본 ======================
    // ======================================================

    public static PostInfo from(Post post, Long authorId, String authorName) {

        return from(post, authorId, authorName, null, null, 0, false);
    }

    public static PostInfo from(Post post, Long authorId, String authorName, int commentCount) {

        return from(post, authorId, authorName, null, null, commentCount, false);
    }

    public static PostInfo from(
        Post post, Long authorId, String authorName,
        String authorProfileImage, int commentCount) {

        return from(post, authorId, authorName, authorProfileImage, null, commentCount, false);
    }

    public static PostInfo from(
        Post post, Long authorId, String authorName, String authorProfileImage,
        ChallengerPart authorPart, int commentCount) {

        return from(post, authorId, authorName, authorProfileImage, authorPart, commentCount, false);
    }

    public static PostInfo from(
        Post post, Long authorId, String authorName, String authorProfileImage,
        ChallengerPart authorPart, int commentCount, boolean isAuthor) {

        Long postId = post.getPostId() != null ? post.getPostId().id() : null;

        // 번개글인 경우
        if (post.isLightning()) {
            Post.LightningInfo info = post.getLightningInfoOrThrow();
            return new PostInfo(
                postId,
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                authorId,
                authorName,
                authorProfileImage,
                authorPart,
                info.meetAt(),
                info.location(),
                info.maxParticipants(),
                info.openChatUrl(),
                post.getCreatedAt(),
                commentCount,
                post.getLikeCount(),
                post.isLiked(),
                isAuthor
            );
        }

        // 일반 게시글
        return new PostInfo(
            postId,
            post.getTitle(),
            post.getContent(),
            post.getCategory(),
            authorId,
            authorName,
            authorProfileImage,
            authorPart,
            null,
            null,
            null,
            null,
            post.getCreatedAt(),
            commentCount,
            post.getLikeCount(),
            post.isLiked(),
            isAuthor
        );
    }
}
