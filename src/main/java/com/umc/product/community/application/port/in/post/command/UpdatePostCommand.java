package com.umc.product.community.application.port.in.post.command;

import com.umc.product.community.domain.enums.Category;

public record UpdatePostCommand(
        Long postId,
        String title,
        String content,
        Category category
) {
}
