package com.umc.product.community.application.port.in.post.query;

import com.umc.product.community.domain.enums.Category;

public record PostSearchQuery(
        Category category
) {
}
