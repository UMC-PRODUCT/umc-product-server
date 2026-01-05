package com.umc.product.organization.application.port.in.dto.paging;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        long totalCount,
        int page,
        int limit
) {
    public long totalPages() {
        return (totalCount + limit - 1) / limit;
    }
}