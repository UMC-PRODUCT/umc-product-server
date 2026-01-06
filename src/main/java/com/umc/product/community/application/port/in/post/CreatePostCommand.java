package com.umc.product.community.application.port.in.post;

import com.umc.product.community.domain.Category;

public record CreatePostCommand(
        String title,
        String content,
        Category category,
        String region,
        boolean anonymous
) {
    // 앞으로 사용이나 정확도를 위해서 여긴 번개랑 분리
}
