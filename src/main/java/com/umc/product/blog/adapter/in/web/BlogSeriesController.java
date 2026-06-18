package com.umc.product.blog.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.blog.adapter.in.web.dto.request.BlogSeriesRequest;
import com.umc.product.blog.adapter.in.web.dto.request.ReplaceBlogSeriesContentsRequest;
import com.umc.product.blog.adapter.in.web.dto.response.BlogContentSummaryResponse;
import com.umc.product.blog.adapter.in.web.dto.response.BlogSeriesResponse;
import com.umc.product.blog.adapter.in.web.dto.response.BlogSeriesSummaryResponse;
import com.umc.product.blog.application.port.in.command.CreateBlogSeriesUseCase;
import com.umc.product.blog.application.port.in.command.DeleteBlogSeriesUseCase;
import com.umc.product.blog.application.port.in.command.ReplaceBlogSeriesContentsUseCase;
import com.umc.product.blog.application.port.in.command.UpdateBlogSeriesUseCase;
import com.umc.product.blog.application.port.in.command.dto.DeleteBlogSeriesCommand;
import com.umc.product.blog.application.port.in.query.GetBlogSeriesUseCase;
import com.umc.product.blog.application.port.in.query.dto.BlogContentCursorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesCursorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesListQuery;
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
@RequestMapping("/api/v1/blog/series")
@RequiredArgsConstructor
@Tag(name = "Blog | 시리즈", description = "블로그 시리즈를 조회하고 관리합니다.")
public class BlogSeriesController {

    private final GetBlogSeriesUseCase getBlogSeriesUseCase;
    private final CreateBlogSeriesUseCase createBlogSeriesUseCase;
    private final UpdateBlogSeriesUseCase updateBlogSeriesUseCase;
    private final DeleteBlogSeriesUseCase deleteBlogSeriesUseCase;
    private final ReplaceBlogSeriesContentsUseCase replaceBlogSeriesContentsUseCase;

    @Public
    @GetMapping
    @Operation(operationId = "BLOG-SERIES-001", summary = "공개 시리즈 목록 조회")
    public CursorResponse<BlogSeriesSummaryResponse> getPublicSeries(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        BlogSeriesCursorInfo info = getBlogSeriesUseCase.getPublicSeries(
            BlogSeriesListQuery.of(type, cursor, size, sort, getNullableMemberId(memberPrincipal))
        );
        List<BlogSeriesSummaryResponse> content = info.content().stream()
            .map(BlogSeriesSummaryResponse::from)
            .toList();
        return CursorResponse.of(content, info.nextCursor(), info.hasNext());
    }

    @Public
    @GetMapping("/{type}/{slug}")
    @Operation(operationId = "BLOG-SERIES-002", summary = "공개 시리즈 상세 조회")
    public BlogSeriesResponse getPublicSeries(
        @PathVariable String type,
        @PathVariable String slug,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return BlogSeriesResponse.from(
            getBlogSeriesUseCase.getPublicSeries(type, slug, getNullableMemberId(memberPrincipal))
        );
    }

    @Public
    @GetMapping("/{type}/{slug}/contents")
    @Operation(operationId = "BLOG-SERIES-003", summary = "공개 시리즈 콘텐츠 목록 조회")
    public CursorResponse<BlogContentSummaryResponse> getPublicSeriesContents(
        @PathVariable String type,
        @PathVariable String slug,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "displayOrder,asc") String sort,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        BlogContentCursorInfo info = getBlogSeriesUseCase.getPublicSeriesContents(
            type,
            slug,
            cursor,
            size,
            sort,
            getNullableMemberId(memberPrincipal)
        );
        return CursorResponse.of(
            info.content().stream().map(BlogContentSummaryResponse::from).toList(),
            info.nextCursor(),
            info.hasNext()
        );
    }

    @GetMapping("/{seriesId}/preview")
    @CheckAccess(resourceType = ResourceType.BLOG_SERIES, resourceId = "#seriesId", permission = PermissionType.READ)
    @Operation(operationId = "BLOG-SERIES-004", summary = "시리즈 미리보기 조회")
    public BlogSeriesResponse getPreview(
        @PathVariable Long seriesId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return BlogSeriesResponse.from(getBlogSeriesUseCase.getPreview(seriesId, requireMemberId(memberPrincipal)));
    }

    @PostMapping
    @CheckAccess(resourceType = ResourceType.BLOG_SERIES, permission = PermissionType.WRITE)
    @Operation(operationId = "BLOG-SERIES-005", summary = "시리즈 생성")
    public BlogSeriesResponse create(
        @Valid @RequestBody BlogSeriesRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return BlogSeriesResponse.from(
            createBlogSeriesUseCase.create(request.toCreateCommand(requireMemberId(memberPrincipal)))
        );
    }

    @PatchMapping("/{seriesId}")
    @CheckAccess(resourceType = ResourceType.BLOG_SERIES, resourceId = "#seriesId", permission = PermissionType.EDIT)
    @Operation(operationId = "BLOG-SERIES-006", summary = "시리즈 수정")
    public BlogSeriesResponse update(
        @PathVariable Long seriesId,
        @Valid @RequestBody BlogSeriesRequest request
    ) {
        return BlogSeriesResponse.from(updateBlogSeriesUseCase.update(request.toUpdateCommand(seriesId)));
    }

    @DeleteMapping("/{seriesId}")
    @CheckAccess(resourceType = ResourceType.BLOG_SERIES, resourceId = "#seriesId", permission = PermissionType.DELETE)
    @Operation(operationId = "BLOG-SERIES-007", summary = "시리즈 삭제")
    public void delete(
        @PathVariable Long seriesId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        deleteBlogSeriesUseCase.delete(DeleteBlogSeriesCommand.of(seriesId, requireMemberId(memberPrincipal)));
    }

    @PutMapping("/{seriesId}/contents")
    @CheckAccess(resourceType = ResourceType.BLOG_SERIES, resourceId = "#seriesId", permission = PermissionType.EDIT)
    @Operation(operationId = "BLOG-SERIES-008", summary = "시리즈 콘텐츠 전체 교체")
    public BlogSeriesResponse replaceContents(
        @PathVariable Long seriesId,
        @Valid @RequestBody ReplaceBlogSeriesContentsRequest request
    ) {
        return BlogSeriesResponse.from(replaceBlogSeriesContentsUseCase.replaceContents(request.toCommand(seriesId)));
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
