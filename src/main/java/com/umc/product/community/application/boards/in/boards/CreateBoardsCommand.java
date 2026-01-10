package com.umc.product.community.application.boards.in.boards;

import com.umc.product.community.domain.Category;

public record CreateBoardsCommand(
        String title,
        String content,
        Category category,
        String region,
        boolean anonymous
) {
    // 앞으로 사용이나 정확도를 위해서 여긴 번개랑 분리
}
