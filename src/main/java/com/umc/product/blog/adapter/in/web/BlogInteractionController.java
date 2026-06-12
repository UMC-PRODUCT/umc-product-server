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
import com.umc.product.blog.adapter.in.web.dto.request.CreateBlogCommentRequest;
import com.umc.product.blog.adapter.in.web.dto.request.UpdateBlogCommentRequest;
import com.umc.product.blog.adapter.in.web.dto.response.BlogCommentResponse;
import com.umc.product.blog.adapter.in.web.dto.response.BlogLikeResponse;
import com.umc.product.blog.application.port.in.command.CreateBlogCommentUseCase;
import com.umc.product.blog.application.port.in.command.DeleteBlogCommentUseCase;
import com.umc.product.blog.application.port.in.command.ToggleBlogCommentLikeUseCase;
import com.umc.product.blog.application.port.in.command.ToggleBlogContentLikeUseCase;
import com.umc.product.blog.application.port.in.command.UpdateBlogCommentUseCase;
import com.umc.product.blog.application.port.in.command.dto.DeleteBlogCommentCommand;
import com.umc.product.blog.application.port.in.query.GetBlogCommentListUseCase;
import com.umc.product.blog.application.port.in.query.GetBlogContentLikeUseCase;
import com.umc.product.blog.application.port.in.query.dto.BlogCommentCursorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogCommentListQuery;
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
@RequestMapping("/api/v1/blog/contents/{type}/{slug}")
@RequiredArgsConstructor
@Tag(name = "Blog | 댓글/좋아요", description = "정적 블로그 콘텐츠 댓글 및 좋아요 API")
public class BlogInteractionController {

    private final GetBlogContentLikeUseCase getBlogContentLikeUseCase;
    private final ToggleBlogContentLikeUseCase toggleBlogContentLikeUseCase;
    private final GetBlogCommentListUseCase getBlogCommentListUseCase;
    private final CreateBlogCommentUseCase createBlogCommentUseCase;
    private final UpdateBlogCommentUseCase updateBlogCommentUseCase;
    private final DeleteBlogCommentUseCase deleteBlogCommentUseCase;
    private final ToggleBlogCommentLikeUseCase toggleBlogCommentLikeUseCase;

    @Public
    @GetMapping("/like")
    @Operation(summary = "[BLOG-001] 콘텐츠 좋아요 상태 조회", description = "콘텐츠 좋아요 수와 현재 사용자의 좋아요 여부를 조회합니다.")
    public BlogLikeResponse getContentLike(
        @PathVariable String type,
        @PathVariable String slug,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long viewerMemberId = getNullableMemberId(memberPrincipal);
        return BlogLikeResponse.from(
            getBlogContentLikeUseCase.getLikeState(type, slug, viewerMemberId)
        );
    }

    @PostMapping("/like")
    @Operation(summary = "[BLOG-002] 콘텐츠 좋아요 토글", description = "콘텐츠 좋아요를 토글합니다.")
    public BlogLikeResponse toggleContentLike(
        @PathVariable String type,
        @PathVariable String slug,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return BlogLikeResponse.from(
            toggleBlogContentLikeUseCase.toggle(type, slug, requireMemberId(memberPrincipal))
        );
    }

    @Public
    @GetMapping("/comments")
    @Operation(summary = "[BLOG-003] 댓글 목록 조회", description = "최상위 댓글을 커서 기반으로 조회하고 1단계 대댓글을 포함합니다.")
    public CursorResponse<BlogCommentResponse> getComments(
        @PathVariable String type,
        @PathVariable String slug,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        BlogCommentCursorInfo info = getBlogCommentListUseCase.getComments(
            BlogCommentListQuery.of(type, slug, cursor, size, sort, getNullableMemberId(memberPrincipal))
        );
        List<BlogCommentResponse> comments = info.content().stream()
            .map(BlogCommentResponse::from)
            .toList();

        return CursorResponse.of(comments, info.nextCursor(), info.hasNext());
    }

    @Public
    @PostMapping("/comments")
    @Operation(summary = "[BLOG-004] 댓글 작성", description = "콘텐츠에 댓글 또는 1단계 대댓글을 작성합니다.")
    public BlogCommentResponse createComment(
        @PathVariable String type,
        @PathVariable String slug,
        @Valid @RequestBody CreateBlogCommentRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return BlogCommentResponse.from(
            createBlogCommentUseCase.create(request.toCommand(type, slug, getNullableMemberId(memberPrincipal)))
        );
    }

    @PatchMapping("/comments/{commentId}")
    @CheckAccess(
        resourceType = ResourceType.BLOG_COMMENT,
        resourceId = "#commentId",
        permission = PermissionType.EDIT,
        message = "본인의 댓글만 수정할 수 있습니다."
    )
    @Operation(summary = "[BLOG-005] 댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
    public BlogCommentResponse updateComment(
        @PathVariable String type,
        @PathVariable String slug,
        @PathVariable Long commentId,
        @Valid @RequestBody UpdateBlogCommentRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return BlogCommentResponse.from(
            updateBlogCommentUseCase.update(request.toCommand(type, slug, commentId, requireMemberId(memberPrincipal)))
        );
    }

    @DeleteMapping("/comments/{commentId}")
    @CheckAccess(
        resourceType = ResourceType.BLOG_COMMENT,
        resourceId = "#commentId",
        permission = PermissionType.DELETE,
        message = "본인 또는 슈퍼 관리자가 댓글을 삭제할 수 있습니다."
    )
    @Operation(summary = "[BLOG-006] 댓글 삭제", description = "작성자 본인 또는 슈퍼 관리자가 댓글을 삭제합니다.")
    public void deleteComment(
        @PathVariable String type,
        @PathVariable String slug,
        @PathVariable Long commentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        deleteBlogCommentUseCase.delete(
            DeleteBlogCommentCommand.of(type, slug, commentId, requireMemberId(memberPrincipal))
        );
    }

    @PostMapping("/comments/{commentId}/like")
    @Operation(summary = "[BLOG-007] 댓글 좋아요 토글", description = "댓글 좋아요를 토글합니다.")
    public BlogLikeResponse toggleCommentLike(
        @PathVariable String type,
        @PathVariable String slug,
        @PathVariable Long commentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return BlogLikeResponse.from(
            toggleBlogCommentLikeUseCase.toggle(type, slug, commentId, requireMemberId(memberPrincipal))
        );
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
