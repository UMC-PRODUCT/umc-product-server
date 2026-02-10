package com.umc.product.community.application.port.in.post.command;

import com.umc.product.community.domain.enums.Category;

public record CreatePostCommand(
        String title,
        String content,
        Category category,
        Long authorChallengerId
) {
}
