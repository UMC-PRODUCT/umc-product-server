package com.umc.product.community.adapter.in.web.dto.request;

import com.umc.product.community.application.port.in.post.command.CreatePostCommand;
import com.umc.product.community.domain.enums.Category;
import java.util.Objects;

public record CreatePostRequest(
        String title,
        String content,
        Category category,
        String region,
        boolean anonymous
) {
    public CreatePostRequest {
        Objects.requireNonNull(title, "제목은 필수입니다");
        Objects.requireNonNull(content, "내용은 필수입니다");
        Objects.requireNonNull(category, "카테고리는 필수입니다");
        Objects.requireNonNull(region, "지역은 필수입니다");

        if (title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다");
        }
        if (content.isBlank()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다");
        }
        if (region.isBlank()) {
            throw new IllegalArgumentException("지역은 비어있을 수 없습니다");
        }
        if (category == Category.LIGHTNING) {
            throw new IllegalArgumentException("번개글은 별도 API를 사용하세요");
        }
    }

    public CreatePostCommand toCommand() {
        return new CreatePostCommand(title, content, category, region, anonymous);
    }
}
