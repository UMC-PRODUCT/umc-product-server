package com.umc.product.blog.adapter.in.web.dto.request;

import java.util.List;

import com.umc.product.blog.application.port.in.command.dto.CreateBlogContentCommand;
import com.umc.product.blog.application.port.in.command.dto.UpdateBlogContentCommand;
import com.umc.product.blog.domain.BlogContentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogContentRequest(
    @NotBlank(message = "type은 필수입니다.") String type,

    @NotBlank(message = "slug는 필수입니다.") @Size(max = 200, message = "slug는 최대 200자까지 입력할 수 있습니다.") String slug,

    @NotBlank(message = "제목은 필수입니다.") @Size(max = 200, message = "제목은 최대 200자까지 입력할 수 있습니다.") String title,

    @Size(max = 500, message = "요약은 최대 500자까지 입력할 수 있습니다.") String summary,

    @Size(max = 1000, message = "썸네일 URL은 최대 1000자까지 입력할 수 있습니다.") String thumbnailUrl,

    @NotBlank(message = "본문은 필수입니다.") @Size(max = 100000, message = "본문은 최대 100000자까지 입력할 수 있습니다.") String content,

    BlogContentStatus status,

    @Size(max = 200, message = "SEO 제목은 최대 200자까지 입력할 수 있습니다.") String seoTitle,

    @Size(max = 500, message = "SEO 설명은 최대 500자까지 입력할 수 있습니다.") String seoDescription,

    @Size(max = 1000, message = "OG 이미지 URL은 최대 1000자까지 입력할 수 있습니다.") String ogImageUrl,

    List<String> hashtags
) {
    public BlogContentRequest {
        type = normalize(type);
        slug = normalize(slug);
        title = normalize(title);
        summary = normalize(summary);
        thumbnailUrl = normalize(thumbnailUrl);
        content = normalize(content);
        seoTitle = normalize(seoTitle);
        seoDescription = normalize(seoDescription);
        ogImageUrl = normalize(ogImageUrl);
        hashtags = hashtags == null ? List.of() : hashtags.stream().map(BlogContentRequest::normalize).toList();
    }

    public CreateBlogContentCommand toCreateCommand(Long authorMemberId) {
        return CreateBlogContentCommand.of(type, slug, title, summary, thumbnailUrl, content, status, authorMemberId,
            seoTitle, seoDescription, ogImageUrl, hashtags);
    }

    public UpdateBlogContentCommand toUpdateCommand(Long contentId) {
        return UpdateBlogContentCommand.of(contentId, slug, title, summary, thumbnailUrl, content, status, seoTitle,
            seoDescription, ogImageUrl, hashtags);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
