package com.umc.product.community.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.domain.enums.Category;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PostDetailInfo(
    Long postId,
    String title,
    String content,
    Category category,
    Long authorId,
    Long authorMemberId,
    Long authorChallengerId,
    String authorName,
    String authorNickname,
    String authorProfileImage,
    ChallengerPart authorPart,
    Instant meetAt,
    String location,
    Integer maxParticipants,
    String openChatUrl,
    int commentCount,
    Instant createdAt,
    int likeCount,
    boolean isLiked,
    boolean isScrapped,
    int scrapCount
) {
    public static PostDetailInfo of(
        PostInfo postInfo, int commentCount, ChallengerPart authorPart,
        boolean isScrapped, int scrapCount
    ) {
        return PostDetailInfo.builder()
            .postId(postInfo.postId())
            .title(postInfo.title())
            .content(postInfo.content())
            .category(postInfo.category())
            .authorId(postInfo.authorChallengerId())
            .authorMemberId(postInfo.authorMemberId())
            .authorChallengerId(postInfo.authorChallengerId())
            .authorName(postInfo.authorName())
            .authorNickname(postInfo.authorNickname())
            .authorProfileImage(postInfo.authorProfileImage())
            .authorPart(authorPart)
            .meetAt(postInfo.meetAt())
            .location(postInfo.location())
            .maxParticipants(postInfo.maxParticipants())
            .openChatUrl(postInfo.openChatUrl())
            .commentCount(commentCount)
            .createdAt(postInfo.createdAt())
            .likeCount(postInfo.likeCount())
            .isLiked(postInfo.isLiked())
            .isScrapped(isScrapped)
            .scrapCount(scrapCount)
            .build();
    }
}
