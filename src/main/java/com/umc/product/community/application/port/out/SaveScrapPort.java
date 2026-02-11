package com.umc.product.community.application.port.out;

import com.umc.product.community.domain.Scrap;

public interface SaveScrapPort {

    Scrap save(Scrap scrap);

    void delete(Scrap scrap);

    void deleteByPostIdAndChallengerId(Long postId, Long challengerId);
}
