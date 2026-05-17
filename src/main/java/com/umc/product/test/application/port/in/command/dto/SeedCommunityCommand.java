package com.umc.product.test.application.port.in.command.dto;

/**
 * Community 시딩 Command. ADR-017 참조.
 *
 * @param gisuId          작성자 챌린저를 추출할 기수 (null 이면 활성 기수)
 * @param postCount       생성할 Post 수 (0 이하면 Post 스킵)
 * @param commentsPerPost 게시글당 생성할 댓글 수 (0 이하면 Comment 스킵)
 * @param trophyCount     생성할 Trophy 수 (0 이하면 Trophy 스킵)
 */
public record SeedCommunityCommand(
    Long gisuId,
    int postCount,
    int commentsPerPost,
    int trophyCount
) {
}
