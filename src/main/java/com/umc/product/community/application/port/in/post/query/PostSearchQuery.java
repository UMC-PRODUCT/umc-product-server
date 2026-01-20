package com.umc.product.community.application.port.in.post.query;

import com.umc.product.community.domain.enums.PostSortType;

public record PostSearchQuery(
        boolean ing,        // 모집중
        PostSortType sort,  // Soft, Hard, ALL
        Integer page,
        Integer size
) {
}
