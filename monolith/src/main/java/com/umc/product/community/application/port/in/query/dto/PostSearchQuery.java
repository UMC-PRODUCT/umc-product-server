package com.umc.product.community.application.port.in.query.dto;

import com.umc.product.community.domain.enums.Category;

public record PostSearchQuery(
    Category category
) {
}
