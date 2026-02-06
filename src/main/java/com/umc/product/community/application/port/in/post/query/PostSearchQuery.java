package com.umc.product.community.application.port.in.post.query;

import com.umc.product.community.domain.enums.PostSortType;

public record PostSearchQuery(
        PostSortType sort
) {
}
