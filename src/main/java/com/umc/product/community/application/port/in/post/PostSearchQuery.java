package com.umc.product.community.application.port.in.post;

import com.umc.product.community.domain.PostSortType;

public record PostSearchQuery(
        boolean ing,        // 모집중
        PostSortType sort,  // Soft, Hard, ALL
        int page,
        int size
) {
}
