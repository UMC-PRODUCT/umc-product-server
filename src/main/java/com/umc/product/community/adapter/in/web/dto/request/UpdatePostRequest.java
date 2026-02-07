package com.umc.product.community.adapter.in.web.dto.request;

import com.umc.product.community.application.port.in.post.command.UpdatePostCommand;
import com.umc.product.community.domain.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "게시글 수정 요청")
public record UpdatePostRequest(
        @Schema(description = "제목", example = "스터디원 모집합니다 (수정)")
        String title,

        @Schema(description = "내용", example = "Spring Boot 스터디원 모집합니다. (수정된 내용)")
        String content,

        @Schema(description = "카테고리", example = "FREE")
        Category category
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
        return new UpdatePostCommand(postId, title, content, category);
    }
}
