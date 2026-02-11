package com.umc.product.community.application.port.out;

/**
 * 스크랩 조회 Port
 */
public interface LoadScrapPort {
    boolean existsByPostIdAndChallengerId(Long postId, Long challengerId);
}
