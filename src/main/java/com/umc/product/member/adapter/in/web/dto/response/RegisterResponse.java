package com.umc.product.member.adapter.in.web.dto.response;

import lombok.Builder;

@Builder
public record RegisterResponse(
        Long memberId
) {
}
