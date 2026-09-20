package com.umc.product.community.application.port.out.scrap;

import java.util.Optional;

import com.umc.product.community.domain.Scrap;

public interface LoadScrapPort {

    Optional<Scrap> findByPostIdAndChallengerId(Long postId, Long challengerId);

    boolean existsByPostIdAndChallengerId(Long postId, Long challengerId);

    int countByPostId(Long postId);
}
