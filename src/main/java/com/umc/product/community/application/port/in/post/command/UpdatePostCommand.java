package com.umc.product.community.application.port.in.post.command;

import com.umc.product.community.domain.enums.Category;

public record UpdatePostCommand(
        Long postId,
        String title,     // 고칠 제목
        String content,  // 고칠 내용
        Category category,
        String region
) {
}
