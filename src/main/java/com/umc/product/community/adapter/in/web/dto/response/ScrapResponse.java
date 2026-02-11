package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.post.ToggleScrapUseCase.ScrapResult;

public record ScrapResponse(
        boolean scrapped
) {
    public static ScrapResponse from(ScrapResult result) {
        return new ScrapResponse(result.scrapped());
    }
}
