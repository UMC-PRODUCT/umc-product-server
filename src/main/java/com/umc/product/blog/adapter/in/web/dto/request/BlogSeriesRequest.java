package com.umc.product.blog.adapter.in.web.dto.request;

import com.umc.product.blog.application.port.in.command.dto.CreateBlogSeriesCommand;
import com.umc.product.blog.application.port.in.command.dto.UpdateBlogSeriesCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogSeriesRequest(
    @NotBlank(message = "type은 필수입니다.") String type,

    @NotBlank(message = "slug는 필수입니다.") @Size(max = 200, message = "slug는 최대 200자까지 입력할 수 있습니다.") String slug,

    @NotBlank(message = "제목은 필수입니다.") @Size(max = 200, message = "제목은 최대 200자까지 입력할 수 있습니다.") String title,

    @Size(max = 1000, message = "설명은 최대 1000자까지 입력할 수 있습니다.") String description,

    @Size(max = 1000, message = "썸네일 URL은 최대 1000자까지 입력할 수 있습니다.") String thumbnailUrl,

    @Size(max = 200, message = "SEO 제목은 최대 200자까지 입력할 수 있습니다.") String seoTitle,

    @Size(max = 500, message = "SEO 설명은 최대 500자까지 입력할 수 있습니다.") String seoDescription,

    @Size(max = 1000, message = "OG 이미지 URL은 최대 1000자까지 입력할 수 있습니다.") String ogImageUrl
) {
    public BlogSeriesRequest {
        type = normalize(type);
        slug = normalize(slug);
        title = normalize(title);
        description = normalize(description);
        thumbnailUrl = normalize(thumbnailUrl);
        seoTitle = normalize(seoTitle);
        seoDescription = normalize(seoDescription);
        ogImageUrl = normalize(ogImageUrl);
    }

    public CreateBlogSeriesCommand toCreateCommand(Long authorMemberId) {
        return CreateBlogSeriesCommand.of(type, slug, title, description, thumbnailUrl, authorMemberId, seoTitle,
            seoDescription, ogImageUrl);
    }

    public UpdateBlogSeriesCommand toUpdateCommand(Long seriesId) {
        return UpdateBlogSeriesCommand.of(seriesId, slug, title, description, thumbnailUrl, seoTitle, seoDescription,
            ogImageUrl);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
