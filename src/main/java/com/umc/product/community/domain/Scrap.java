package com.umc.product.community.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Scrap {
    private final ScrapId scrapId;
    private final Long postId;
    private final Long challengerId;

    public static Scrap create(Long postId, Long challengerId) {
        validatePostId(postId);
        validateChallengerId(challengerId);
        return new Scrap(null, postId, challengerId);
    }

    public static Scrap reconstruct(ScrapId scrapId, Long postId, Long challengerId) {
        return new Scrap(scrapId, postId, challengerId);
    }

    private static void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("게시글 ID는 필수이며 양수여야 합니다.");
        }
    }

    private static void validateChallengerId(Long challengerId) {
        if (challengerId == null || challengerId <= 0) {
            throw new IllegalArgumentException("챌린저 ID는 필수이며 양수여야 합니다.");
        }
    }

    public record ScrapId(Long id) {
        public ScrapId {
            if (id <= 0) {
                throw new IllegalArgumentException("ID는 양수여야 합니다.");
            }
        }
    }
}
