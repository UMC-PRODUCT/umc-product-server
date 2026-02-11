package com.umc.product.community.application.port.in.post;

public interface ToggleScrapUseCase {

    ScrapResult toggle(Long postId, Long challengerId);

    record ScrapResult(
            boolean scrapped,
            int scrapCount
    ) {
    }
}
