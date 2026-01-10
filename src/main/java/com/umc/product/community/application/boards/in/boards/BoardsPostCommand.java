package com.umc.product.community.application.boards.in.boards;

public record BoardsPostCommand(
        Long postId,
        String title,     // 고칠 제목
        String content    // 고칠 내용
) {
}
