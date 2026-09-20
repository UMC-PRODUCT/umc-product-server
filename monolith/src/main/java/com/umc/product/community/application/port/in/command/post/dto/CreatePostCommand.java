package com.umc.product.community.application.port.in.command.post.dto;

import com.umc.product.community.domain.enums.Category;

public record CreatePostCommand(
    String title,
    String content,
    Category category,
    Long authorChallengerId
) {
}
