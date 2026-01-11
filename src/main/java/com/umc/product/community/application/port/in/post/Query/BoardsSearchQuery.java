package com.umc.product.community.application.port.in.post.Query;

import com.umc.product.community.domain.BoardsSortType;

public record BoardsSearchQuery(
        boolean ing,        // 모집중
        BoardsSortType sort,  // Soft, Hard, ALL
        Integer page,
        Integer size
) {
}
