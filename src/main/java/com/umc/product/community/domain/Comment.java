package com.umc.product.community.domain;

import com.umc.product.community.domain.exception.CommunityDomainException;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class Comment {

    private final CommentId commentId;
    private final Long postId;
    private final Long challengerId;
    private final int likeCount;
    private final boolean liked;
    private final Instant createdAt;
    private String content;

    public static Comment create(
        Long postId, Long challengerId, String content, Long parentId
    ) {
        validateRequired(postId, challengerId, content);

        return Comment.builder()
            .postId(postId)
            .challengerId(challengerId)
            .content(content)
            .likeCount(0)
            .liked(false)
            .createdAt(Instant.now())
            .build();
    }

    public static Comment reconstruct(
        CommentId commentId, Long postId, Long challengerId,
        String content, Long parentId, int likeCount, boolean liked,
        Instant createdAt
    ) {
        return Comment.builder()
            .commentId(commentId)
            .postId(postId)
            .challengerId(challengerId)
            .content(content)
            .likeCount(likeCount)
            .liked(liked)
            .createdAt(createdAt)
            .build();
    }

    private static void validateRequired(Long postId, Long challengerId, String content) {
        if (postId == null || postId <= 0) {
            throw new CommunityDomainException(CommunityErrorCode.INVALID_COMMENT_POST_ID);
        }
        if (challengerId == null || challengerId <= 0) {
            throw new CommunityDomainException(CommunityErrorCode.INVALID_COMMENT_CHALLENGER_ID);
        }
        if (content == null || content.isBlank()) {
            throw new CommunityDomainException(CommunityErrorCode.INVALID_COMMENT_CONTENT);
        }
    }

    public void updateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new CommunityDomainException(CommunityErrorCode.INVALID_COMMENT_CONTENT);
        }
        this.content = content;
    }

    public record CommentId(Long id) {
        public CommentId {
            if (id <= 0) {
                throw new CommunityDomainException(CommunityErrorCode.INVALID_ID);
            }
        }
    }
}
