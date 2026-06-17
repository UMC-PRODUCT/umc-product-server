package com.umc.product.blog.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.blog.adapter.in.web.dto.response.BlogContentSummaryResponse;
import com.umc.product.blog.adapter.in.web.dto.response.BlogHashtagResponse;
import com.umc.product.blog.application.port.in.query.GetBlogHashtagUseCase;
import com.umc.product.blog.application.port.in.query.dto.BlogContentCursorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogHashtagCursorInfo;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Public
@RestController
@RequestMapping("/api/v1/blog/hashtags")
@RequiredArgsConstructor
@Tag(name = "Blog | 해시태그", description = "공개 블로그 해시태그와 연결 콘텐츠를 조회합니다.")
public class BlogHashtagController {

    private final GetBlogHashtagUseCase getBlogHashtagUseCase;

    @GetMapping
    @Operation(operationId = "BLOG-HASHTAG-001", summary = "공개 해시태그 목록 조회")
    public CursorResponse<BlogHashtagResponse> getPublicHashtags(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "contentCount,desc") String sort
    ) {
        BlogHashtagCursorInfo info = getBlogHashtagUseCase.getPublicHashtags(type, q, cursor, size, sort);
        return CursorResponse.of(
            info.content().stream().map(BlogHashtagResponse::from).toList(),
            info.nextCursor(),
            info.hasNext()
        );
    }

    @GetMapping("/{slug}/contents")
    @Operation(operationId = "BLOG-HASHTAG-002", summary = "공개 해시태그 콘텐츠 목록 조회")
    public CursorResponse<BlogContentSummaryResponse> getPublicHashtagContents(
        @PathVariable String slug,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "publishedAt,desc") String sort,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        BlogContentCursorInfo info = getBlogHashtagUseCase.getPublicHashtagContents(
            slug,
            type,
            cursor,
            size,
            sort,
            memberPrincipal == null ? null : memberPrincipal.getMemberId()
        );
        return CursorResponse.of(
            info.content().stream().map(BlogContentSummaryResponse::from).toList(),
            info.nextCursor(),
            info.hasNext()
        );
    }
}
