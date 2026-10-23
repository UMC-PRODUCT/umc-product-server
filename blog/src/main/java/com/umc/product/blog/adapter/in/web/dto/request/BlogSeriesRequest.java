package com.umc.product.blog.adapter.in.web.dto.request;

import com.umc.product.blog.application.port.in.command.dto.CreateBlogSeriesCommand;
import com.umc.product.blog.application.port.in.command.dto.UpdateBlogSeriesCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BlogSeriesRequest(
    @NotBlank(message = "카테고리를 선택해주세요.") String type,

    @NotBlank(message = "주소를 입력해주세요.") @Size(max = 200, message = "주소는 200자 이하로 입력해주세요.") @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "주소는 영문 소문자, 숫자, 하이픈만 사용할 수 있어요.")
    String slug,

    @NotBlank(message = "제목을 입력해주세요.") @Size(max = 200, message = "제목은 200자 이하로 입력해주세요.") String title,

    @Size(max = 1000, message = "설명은 1,000자 이하로 입력해주세요.") String description,

    @Size(max = 1000, message = "썸네일 URL은 1,000자 이하로 입력해주세요.") String thumbnailUrl,

    @Size(max = 200, message = "SEO 제목은 200자 이하로 입력해주세요.") String seoTitle,

    @Size(max = 500, message = "SEO 설명은 500자 이하로 입력해주세요.") String seoDescription,

    @Size(max = 1000, message = "OG 이미지 URL은 1,000자 이하로 입력해주세요.") String ogImageUrl
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
