package com.umc.product.community.application.port.in.post;

public interface ToggleScrapUseCase {
    /**
     * 게시글 스크랩 토글
     *
     * @param postId       게시글 ID
     * @param challengerId 챌린저 ID
     * @return 스크랩 결과
     */
    ScrapResult toggleScrap(Long postId, Long challengerId);

    ScrapResult toggle(Long postId, Long challengerId);

    record ScrapResult(
            boolean scrapped,
            int scrapCount
    ) {
    }
}
