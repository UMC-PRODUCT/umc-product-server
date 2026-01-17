package com.umc.product.challenger.adapter.in.web.dto.request;

public record EditChallengerPointRequest(
        Long challengerPointId,
        String newDescription // 설명 수정
) {
}
