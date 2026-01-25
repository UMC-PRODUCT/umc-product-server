package com.umc.product.global.response;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageRequest(
        // common으로 빼도 괜찮을 거 같습니다
        @Min(1) int page,
        @Min(1) @Max(100) int limit) {
    public long offset() {
        return (long) (page - 1) * limit;
    }
}
