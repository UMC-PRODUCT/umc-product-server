package com.umc.product.blog.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.blog.adapter.in.web.dto.request.BlogContentRequest;
import com.umc.product.blog.adapter.in.web.dto.response.BlogContentResponse;
import com.umc.product.blog.adapter.in.web.dto.response.BlogContentSummaryResponse;
import com.umc.product.blog.adapter.in.web.dto.response.BlogSeoPathsResponse;
import com.umc.product.blog.application.port.in.command.CreateBlogContentUseCase;
import com.umc.product.blog.application.port.in.command.DeleteBlogContentUseCase;
import com.umc.product.blog.application.port.in.command.UpdateBlogContentUseCase;
import com.umc.product.blog.application.port.in.command.dto.DeleteBlogContentCommand;
import com.umc.product.blog.application.port.in.query.GetBlogContentUseCase;
import com.umc.product.blog.application.port.in.query.dto.BlogContentCursorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogContentListQuery;
import com.umc.product.blog.domain.BlogDomainException;
import com.umc.product.blog.domain.BlogErrorCode;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/blog")
@RequiredArgsConstructor
@Tag(name = "Blog | 콘텐츠", description = "블로그 CMS 콘텐츠 API")
public class BlogContentController {

    private final GetBlogContentUseCase getBlogContentUseCase;
    private final CreateBlogContentUseCase createBlogContentUseCase;
    private final UpdateBlogContentUseCase updateBlogContentUseCase;
    private final DeleteBlogContentUseCase deleteBlogContentUseCase;

    @Public
    @GetMapping("/contents")
    @Operation(summary = "[BLOG-CONTENT-001] 공개 콘텐츠 목록 조회")
    public CursorResponse<BlogContentSummaryResponse> getPublicContents(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String seriesSlug,
        @RequestParam(required = false) String hashtagSlug,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "publishedAt,desc") String sort,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        BlogContentCursorInfo info = getBlogContentUseCase.getPublicContents(
            BlogContentListQuery.of(type, seriesSlug, hashtagSlug, cursor, size, sort, getNullableMemberId(memberPrincipal))
        );
        List<BlogContentSummaryResponse> content = info.content().stream()
            .map(BlogContentSummaryResponse::from)
            .toList();
        return CursorResponse.of(content, info.nextCursor(), info.hasNext());
    }

    @Public
    @GetMapping("/contents/{type}/{slug}")
    @Operation(summary = "[BLOG-CONTENT-002] 공개 콘텐츠 상세 조회")
    public BlogContentResponse getPublicContent(
        @PathVariable String type,
        @PathVariable String slug,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return BlogContentResponse.from(
            getBlogContentUseCase.getPublicContent(type, slug, getNullableMemberId(memberPrincipal))
        );
    }

    @GetMapping("/contents/{contentId}/preview")
    @CheckAccess(resourceType = ResourceType.BLOG_CONTENT, resourceId = "#contentId", permission = PermissionType.READ)
    @Operation(summary = "[BLOG-CONTENT-003] 콘텐츠 preview 조회")
    public BlogContentResponse getPreview(
        @PathVariable Long contentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return BlogContentResponse.from(
            getBlogContentUseCase.getPreview(contentId, requireMemberId(memberPrincipal))
        );
    }

    @PostMapping("/contents")
    @CheckAccess(resourceType = ResourceType.BLOG_CONTENT, permission = PermissionType.WRITE)
    @Operation(summary = "[BLOG-CONTENT-004] 콘텐츠 생성")
    public BlogContentResponse create(
        @Valid @RequestBody BlogContentRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return BlogContentResponse.from(
            createBlogContentUseCase.create(request.toCreateCommand(requireMemberId(memberPrincipal)))
        );
    }

    @PatchMapping("/contents/{contentId}")
    @CheckAccess(resourceType = ResourceType.BLOG_CONTENT, resourceId = "#contentId", permission = PermissionType.EDIT)
    @Operation(summary = "[BLOG-CONTENT-005] 콘텐츠 수정")
    public BlogContentResponse update(
        @PathVariable Long contentId,
        @Valid @RequestBody BlogContentRequest request
    ) {
        return BlogContentResponse.from(updateBlogContentUseCase.update(request.toUpdateCommand(contentId)));
    }

    @DeleteMapping("/contents/{contentId}")
    @CheckAccess(resourceType = ResourceType.BLOG_CONTENT, resourceId = "#contentId", permission = PermissionType.DELETE)
    @Operation(summary = "[BLOG-CONTENT-006] 콘텐츠 삭제")
    public void delete(
        @PathVariable Long contentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        deleteBlogContentUseCase.delete(DeleteBlogContentCommand.of(contentId, requireMemberId(memberPrincipal)));
    }

    @Public
    @GetMapping("/seo/paths")
    @Operation(summary = "[BLOG-CONTENT-007] SEO public path 목록 조회")
    public BlogSeoPathsResponse getSeoPaths() {
        return BlogSeoPathsResponse.from(getBlogContentUseCase.getSeoPaths());
    }

    private Long getNullableMemberId(MemberPrincipal memberPrincipal) {
        return memberPrincipal == null ? null : memberPrincipal.getMemberId();
    }

    private Long requireMemberId(MemberPrincipal memberPrincipal) {
        Long memberId = getNullableMemberId(memberPrincipal);
        if (memberId == null) {
            throw new BlogDomainException(BlogErrorCode.INVALID_MEMBER_ID);
        }
        return memberId;
    }
}
