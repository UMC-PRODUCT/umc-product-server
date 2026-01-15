package com.umc.product.community.application.port.in.post.Command;

public record UpdatePostCommand(
        Long postId,
        String title,     // 고칠 제목
        String content    // 고칠 내용
) {
}
