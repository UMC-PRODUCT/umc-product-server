package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.post.ToggleScrapUseCase;

public record ScrapResponse(
        boolean scrapped,
        int scrapCount
) {
    public static ScrapResponse from(ToggleScrapUseCase.ScrapResult result) {
        return new ScrapResponse(result.scrapped(), result.scrapCount());
    }
}
