package com.umc.product.community.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.application.port.in.command.post.dto.PostInfo;
import com.umc.product.community.domain.enums.Category;
import java.time.Instant;
import java.time.LocalDateTime;

public record PostDetailInfo(
    Long postId,
    String title,
    String content,
    Category category,
    Long authorId,
    String authorName,
    String authorProfileImage,
    ChallengerPart authorPart,
    LocalDateTime meetAt,
    String location,
    Integer maxParticipants,
    String openChatUrl,
    int commentCount,
    Instant createdAt,
    int likeCount,
    boolean isLiked,
    boolean isAuthor,
    boolean isScrapped,
    int scrapCount
) {
    public static PostDetailInfo of(PostInfo postInfo, int commentCount, ChallengerPart authorPart,
                                    boolean isScrapped, int scrapCount) {
        return new PostDetailInfo(
            postInfo.postId(),
            postInfo.title(),
            postInfo.content(),
            postInfo.category(),
            postInfo.authorId(),
            postInfo.authorName(),
            postInfo.authorProfileImage(),
            authorPart,
            postInfo.meetAt(),
            postInfo.location(),
            postInfo.maxParticipants(),
            postInfo.openChatUrl(),
            commentCount,
            postInfo.createdAt(),
            postInfo.likeCount(),
            postInfo.isLiked(),
            postInfo.isAuthor(),
            isScrapped,
            scrapCount
        );
    }
}
