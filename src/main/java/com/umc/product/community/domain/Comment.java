package com.umc.product.community.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Comment {

    private final CommentId commentId;
    private final Long postId;
    private final Long challengerId;
    private String content;
    private final int likeCount;
    private final boolean liked;

    public static Comment create(Long postId, Long challengerId, String content, Long parentId) {
        validateRequired(postId, challengerId, content);
        return new Comment(null, postId, challengerId, content, 0, false);
    }

    public static Comment reconstruct(CommentId commentId, Long postId, Long challengerId,
                                      String content, Long parentId, int likeCount, boolean liked) {
        return new Comment(commentId, postId, challengerId, content, likeCount, liked);
    }

    public void updateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
        this.content = content;
    }

    private static void validateRequired(Long postId, Long challengerId, String content) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("게시글 ID는 필수입니다.");
        }
        if (challengerId == null || challengerId <= 0) {
            throw new IllegalArgumentException("챌린저 ID는 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
    }

    public record CommentId(Long id) {
        public CommentId {
            if (id <= 0) {
                throw new IllegalArgumentException("ID는 양수여야 합니다.");
            }
        }
    }
}
