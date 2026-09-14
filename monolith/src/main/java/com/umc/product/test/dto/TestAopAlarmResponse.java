package com.umc.product.test.dto;

import lombok.Builder;

@Builder
public record TestAopAlarmResponse(
    String content
) {
}
