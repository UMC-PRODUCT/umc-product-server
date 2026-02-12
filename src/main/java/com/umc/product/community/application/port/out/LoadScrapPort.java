package com.umc.product.community.application.port.out;

import com.umc.product.community.domain.Scrap;
import java.util.Optional;

public interface LoadScrapPort {

    Optional<Scrap> findByPostIdAndChallengerId(Long postId, Long challengerId);

    boolean existsByPostIdAndChallengerId(Long postId, Long challengerId);

    int countByPostId(Long postId);
}
