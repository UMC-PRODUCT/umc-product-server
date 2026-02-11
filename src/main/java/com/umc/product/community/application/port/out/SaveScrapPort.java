package com.umc.product.community.application.port.out;

/**
 * 스크랩 저장 Port
 */
public interface SaveScrapPort {
    /**
     * 스크랩 토글
     *
     * @param postId       게시글 ID
     * @param challengerId 챌린저 ID
     * @return true: 스크랩됨, false: 스크랩 취소됨
     */
    boolean toggleScrap(Long postId, Long challengerId);
}
