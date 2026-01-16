package com.umc.product.community.adapter.in.web.dto.request;

import com.umc.product.community.application.port.in.post.Command.UpdatePostCommand;
import com.umc.product.community.domain.enums.Category;
import java.util.Objects;

public record UpdatePostRequest(
        String title,
        String content,
        Category category,
        String region
) {
    public UpdatePostRequest {
        Objects.requireNonNull(title, "제목은 필수입니다");
        Objects.requireNonNull(content, "내용은 필수입니다");

        if (title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다");
        }
        if (content.isBlank()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다");
        }
    }

    public UpdatePostCommand toCommand(Long postId) {
        return new UpdatePostCommand(postId, title, content, category, region);
    }
}
