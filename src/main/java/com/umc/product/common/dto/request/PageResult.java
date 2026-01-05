package com.umc.product.common.dto.request;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        long totalCount,
        int page,
        int limit) {
    public long totalPages() {
        return (totalCount + limit - 1) / limit;
    }
}