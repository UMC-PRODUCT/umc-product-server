package com.umc.product.community.application.boards.in.boards;

import com.umc.product.community.domain.BoardsSortType;

public record BoardsSearchQuery(
        boolean ing,        // 모집중
        BoardsSortType sort,  // Soft, Hard, ALL
        int page,
        int size
) {
}
